#include <jni.h>
#include <string>
#include "dobby.h"
#include "Utils.h"
#include <android/log.h>
#include <execinfo.h>
#include <utility>
#include <unwind.h>
#include <dlfcn.h>
#include <iomanip>
#include <unistd.h>
#include <sstream>
#include <array>

JavaVM* g_JavaVM = nullptr;
JNIEnv* test_env = nullptr;
void *array[10];

namespace {


    struct BacktraceState
    {
        void** current;
        void** end;
    };


    static _Unwind_Reason_Code unwindCallback(struct _Unwind_Context* context, void* arg)
    {
        BacktraceState* state = static_cast<BacktraceState*>(arg);
        uintptr_t pc = _Unwind_GetIP(context);
        if (pc) {
            if (state->current == state->end) {
                return _URC_END_OF_STACK;
            } else {
                *state->current++ = reinterpret_cast<void*>(pc);
            }
        }
        return _URC_NO_REASON;
    }
}


size_t captureBacktrace(void** buffer, size_t max)
{
    BacktraceState state = {buffer, buffer + max};
    _Unwind_Backtrace(unwindCallback, &state);
    return state.current - buffer;
}


void dumpBacktrace(std::ostream& os, void** buffer, size_t count)
{
    for (size_t idx = 0; idx < count; ++idx) {
        const void* addr = buffer[idx];
        const char* symbol = "";
        Dl_info info;
        if (dladdr(addr, &info) && info.dli_sname) {
            symbol = info.dli_sname;
        }
        os << "  #" << std::setw(2) << idx << ": " << addr << "  " << symbol << "\n";
    }
}


void backtraceToLogcat()
{
    const size_t max = 100; // 调用的层数
    void* buffer[max];
    std::ostringstream oss;


    dumpBacktrace(oss, buffer, captureBacktrace(buffer, max));


    __android_log_print(ANDROID_LOG_INFO, "zzzccc", "%s", oss.str().c_str());
}

JNIEnv* GetJNIEnv() {
    JNIEnv* env = nullptr;
    // 如果当前线程未附加到 JVM，附加它并获取 JNIEnv
    if (g_JavaVM->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        jint res = g_JavaVM->AttachCurrentThread(&env, nullptr);
        if (res != JNI_OK) {
            // 附加线程失败
            return nullptr;
        }
        // 现在你有了 JNIEnv 指针
    }
    return env;
}

// 打印 Java 层的堆栈跟踪
void CallJavaPrintStackTrace(JNIEnv* env) {
    if(env == nullptr){
        __android_log_print(ANDROID_LOG_ERROR, "CallJavaPrintStackTrace", "JNIEnv is null");
        return;
    }

    // Find the java.lang.Exception class.
    jclass exceptionClass = env->FindClass("java/lang/Exception");
    if (exceptionClass == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CallJavaPrintStackTrace", "Failed to find java.lang.Exception");
        return;
    }

    // Get the ID of the constructor that takes no arguments.
    jmethodID exceptionConstructor = env->GetMethodID(exceptionClass, "<init>", "()V");
    if (exceptionConstructor == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CallJavaPrintStackTrace", "Failed to find the Exception constructor");
        env->DeleteLocalRef(exceptionClass);
        return;
    }

    // Create an instance of Exception.
    jobject exceptionObj = env->NewObject(exceptionClass, exceptionConstructor);
    if (exceptionObj == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CallJavaPrintStackTrace", "Failed to create Exception instance");
        env->DeleteLocalRef(exceptionClass);
        return;
    }

    // Get the ID of the printStackTrace() method.
    jmethodID printStackTraceMethod = env->GetMethodID(exceptionClass, "printStackTrace", "()V");
    if (printStackTraceMethod == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, "CallJavaPrintStackTrace", "Failed to find printStackTrace method");
        env->DeleteLocalRef(exceptionClass);
        env->DeleteLocalRef(exceptionObj);
        return;
    }

    // Call the printStackTrace() method on the Exception instance.
    env->CallVoidMethod(exceptionObj, printStackTraceMethod);

    // Clean up local references.
    env->DeleteLocalRef(exceptionClass);
    env->DeleteLocalRef(exceptionObj);
}

void NativeStackTrace_printNativeStackTrace(JNIEnv *env) {
    // 获取 Thread 类
    jclass threadClass =env->FindClass("java/lang/Thread");
    if (threadClass == nullptr) {
        return; // 类没有找到
    }

    // 获取 currentThread 静态方法的 ID
    jmethodID currentThreadMethod =env->GetStaticMethodID(threadClass, "currentThread", "()Ljava/lang/Thread;");
    if (currentThreadMethod == nullptr) {
        return; // 方法没有找到
    }

    // 调用 currentThread 方法获取当前线程的实例
    jobject currentThread =env->CallStaticObjectMethod(threadClass, currentThreadMethod);
    if (currentThread == nullptr) {
        return; // 当前线程没有获取到
    }

    // 获取 getStackTrace 方法的 ID
    jmethodID getStackTraceMethod =env->GetMethodID(threadClass, "getStackTrace", "()[Ljava/lang/StackTraceElement;");
    if (getStackTraceMethod == nullptr) {
        return; // 方法没有找到
    }

    // 调用 getStackTrace 方法获取当前线程的堆栈跟踪
    jobjectArray stackTrace = (jobjectArray)env->CallObjectMethod( currentThread, getStackTraceMethod);
    if (stackTrace == nullptr) {
        return; // 堆栈跟踪没有获取到
    }

    // 获取堆栈跟踪元素数组的长度
    jsize stackTraceLength =env->GetArrayLength(stackTrace);

    // 获取 StackTraceElement 类
    jclass stackTraceElementClass =env->FindClass("java/lang/StackTraceElement");
    if (stackTraceElementClass == nullptr) {
        return; // 类没有找到
    }

    // 获取 StackTraceElement 中的 toString 方法
    jmethodID toStringMethod =env->GetMethodID(stackTraceElementClass, "toString", "()Ljava/lang/String;");
    if (toStringMethod == nullptr) {
        return; // 方法没有找到
    }

    // 遍历堆栈元素
    for (int i = 0; i < stackTraceLength; i++) {
        jobject stackTraceElement =env->GetObjectArrayElement(stackTrace, i);
        jstring stackTraceString = (jstring)env->CallObjectMethod(stackTraceElement, toStringMethod);

        // 将 Java 字符串转换为 C 字符串
        const char *nativeString =env->GetStringUTFChars(stackTraceString, 0);

        // 打印堆栈跟踪元素
        __android_log_print(6,"zzzccc","the stack is %s",nativeString);

        // 释放 Java 字符串资源
       env->ReleaseStringUTFChars(stackTraceString, nativeString);
       env->DeleteLocalRef(stackTraceElement);
    }
}

void Log_wtf(JNIEnv* env, const char* tag, const char* msg) {
    jclass logClass = env->FindClass("android/util/Log");
    if (logClass == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "zzzccc", "Unable to find Log class");
        return;
    }

    jmethodID wtfId = env->GetStaticMethodID(logClass, "wtf", "(Ljava/lang/String;Ljava/lang/String;)I");
    if (wtfId == NULL) {
        __android_log_print(ANDROID_LOG_ERROR, "zzzccc", "Unable to find Log.wtf method");
        return;
    }

    jstring jtag = env->NewStringUTF(tag);
    jstring jmsg = env->NewStringUTF(msg);

    env->CallStaticIntMethod(logClass, wtfId, jtag, jmsg);

    env->DeleteLocalRef(jtag);
    env->DeleteLocalRef(jmsg);
    env->DeleteLocalRef(logClass);
}
int (*old___system_property_get)(const char* , char* ) = nullptr;

int new___system_property_get(const char* name, char* value) {

    JNIEnv* env = GetJNIEnv();
    int result = old___system_property_get(name, value);
    if(strcmp(name, "ro.serialno") == 0) {
        // The parameter name is equal to "ro.serialno"
        // You might want to do something specific here.
        //abort();
        raise(SIGALRM);
        backtraceToLogcat();
        CallJavaPrintStackTrace(env);
        NativeStackTrace_printNativeStackTrace(env);
        Log_wtf(env,"zzzccc","Logwtf successfully excuted");
        __android_log_print(6,"zzzccc","the result is %p",env);
        __android_log_print(6,"zzzccc","the result is %p",test_env);
        // __android_log_print(ANDROID_LOG_INFO, "StackTrace", "Function: %s, File: %s, Line: %d", __PRETTY_FUNCTION__, __FILE__, __LINE__);
        __android_log_print(6,"zzzccc","the result is %d",result);
        __android_log_print(6,"zzzccc","the value is %s",value);
        __android_log_print(6,"zzzccc","the param is %s",name);
    }
    return result;
}

jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(6, "zzzccc", "zzzccc xposed开始加载");
    JNIEnv *env = nullptr;
    g_JavaVM = vm;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
        test_env = env;
        __android_log_print(6, "zzzccc", "so inject successfully");
        void *__system_property_get_addr = DobbySymbolResolver("/system/lib/libc.so", "__system_property_get");
        __android_log_print(6, "zzzccc", "__system_property_get -> %x ",__system_property_get_addr);
        DobbyHook(__system_property_get_addr, (dobby_dummy_func_t) new___system_property_get, (dobby_dummy_func_t *) &old___system_property_get);
        return JNI_VERSION_1_6;
    }
    return 0;
}
