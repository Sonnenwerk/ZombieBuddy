cmake_minimum_required(VERSION 3.15)
project(zbNative C)

set(CMAKE_C_STANDARD 99)

find_package(JNI REQUIRED)

include_directories(${JNI_INCLUDE_DIRS})

add_library(zbNative SHARED zbNative.c zbNative.def)
target_link_libraries(zbNative PRIVATE ${JNI_LIBRARIES})
