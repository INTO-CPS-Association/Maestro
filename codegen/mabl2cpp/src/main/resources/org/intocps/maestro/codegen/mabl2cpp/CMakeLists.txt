cmake_minimum_required (VERSION 3.11)
project (sim C CXX)


include(FetchContent)
include(ExternalProject)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

project(zlib-download NONE)

FetchContent_Declare(intocpsfmi
  GIT_REPOSITORY git@github.com:INTO-CPS-Association/org.intocps.maestro.fmi.git
        GIT_TAG  Release/1.0.12
        GIT_SHALLOW ON
 SOURCE_DIR "${CMAKE_BINARY_DIR}/intocpsfmi-src"
)

FetchContent_Populate(intocpsfmi)
set(intocpsfmi-src "intocpsfmi-src")

FetchContent_Declare(libzip
        GIT_REPOSITORY https://github.com/nih-at/libzip.git
        GIT_TAG  v1.7.3
        GIT_SHALLOW ON
        SOURCE_DIR "${CMAKE_BINARY_DIR}/libzip"
)

FetchContent_MakeAvailable(libzip )

SET(RAPIDJSON_BUILD_EXAMPLES OFF CACHE BOOL "Build rapidjson examples" FORCE)
SET(RAPIDJSON_BUILD_TESTS OFF CACHE BOOL "Build rapidjson perftests and unittests" FORCE)
FetchContent_Declare(rapidjson
        GIT_REPOSITORY https://github.com/Tencent/rapidjson.git
        GIT_TAG v1.1.0
     #   GIT_SHALLOW ON
        SOURCE_DIR "${CMAKE_BINARY_DIR}/rapidjson"
        BUILD_COMMAND ""
        CONFIGURE_COMMAND ""
        INSTALL_COMMAND ""
        TEST_COMMAND ""
        )

FetchContent_Populate(rapidjson)

#file(CHMOD_RECURSE "${CMAKE_BINARY_DIR}/rapidjson" PERMISSIONS OWNER_READ OWNER_WRITE  GROUP_READ GROUP_WRITE WORLD_READ WORLD_WRITE )

include_directories(${PROJECT_SOURCE_DIR}
#    ${intocpsfmi-src}/jnifmuapi/src/main/native/src/external/shared/fmi2/headers
${intocpsfmi-src}/jnifmuapi/src/main/native/src/external/shared/fmi/include
    ${intocpsfmi-src}/jnifmuapi/src/main/native/src/external/shared
    ${intocpsfmi-src}/jnifmuapi/src/main/native/src
    libs
    ${CMAKE_BINARY_DIR}/rapidjson/include)


add_executable(sim
    ${intocpsfmi-src}/jnifmuapi/src/main/native/src/sim_support.c
    libs/DataWriter.cpp
    libs/BooleanLogic.cpp
    libs/Logger.cpp
    libs/SimFmi2.cpp
    libs/SimMath.cpp
    libs/MEnv.cpp
    libs/unzip.cpp
    co-sim.cxx
    main.cpp)

target_link_libraries(sim zip)
target_compile_definitions(sim PRIVATE -DFMI_COSIMULATION)