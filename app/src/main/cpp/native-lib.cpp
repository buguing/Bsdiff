#include <jni.h>
#include <string>

extern "C" {
extern int execute_patch(int argc, char *argv[]);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_wellee_bsdiff_BsPatchUtil_patch(JNIEnv *env, jclass thiz,jstring oldApk_,
                                          jstring patch_, jstring output_) {

    //将java字符串转换成char指针
    const char *oldApk = env->GetStringUTFChars(oldApk_, 0);
    const char *patch = env->GetStringUTFChars(patch_, 0);
    const char *output = env->GetStringUTFChars(output_, 0);
    //bspatch ,oldfile ,newfile ,patchfile
    char *argv[] = {"", const_cast<char *>(oldApk), const_cast<char *>(output),
                    const_cast<char *>(patch)};
    execute_patch(4, argv);

    // 释放相应的指针gc
    env->ReleaseStringUTFChars(oldApk_, oldApk);
    env->ReleaseStringUTFChars(patch_, patch);
    env->ReleaseStringUTFChars(output_, output);
}
