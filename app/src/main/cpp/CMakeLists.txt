
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Declares and names the project.


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
file(GLOB RTMP_SRC rtmp/*.c)
file(GLOB RTMP_PUSHER pusher/*.cpp)

include_directories(${CMAKE_SOURCE_DIR}/include)

# 通过设置编译选项, 设置函数库的搜索路径
# 此处的 ANDROID_ABI 是在
# build.gradle android->defaultConfig->externalNativeBuild->cmake
# 下的 abiFilters 中设置

# 添加库 编码库
#add_library(x264 SHARED IMPORTED)
# 导入第三方函数库路径配置---配置库
#set_target_properties(x264 PROPERTIES IMPORTED_LOCATION ${JNI_LIBS}/${ANDROID_ABI}/libx264.so)

add_library( # Sets the name of the library.
        rtmp_media

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${RTMP_PUSHER}
        ${RTMP_SRC}
        rtmp_media.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        rtmp_media
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
