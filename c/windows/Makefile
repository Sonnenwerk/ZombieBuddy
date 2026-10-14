NAME=zbNative
ARCH=x86_64

CXX=$(ARCH)-w64-mingw32-g++
CXXFLAGS=-Wall -Os
LDFLAGS=-shared -Wl,--gc-sections -Wl,--strip-all -nostdlib -Wl,--entry=DllMain

TARGET1=$(NAME).dll
SOURCES1=$(NAME).cpp
OBJECTS1=$(SOURCES1:.cpp=.o)
DEF1=$(NAME).def

all: $(TARGET1)

$(TARGET1): $(OBJECTS1)
	$(CXX) $(LDFLAGS) -o $@ $^ $(DEF1) -lkernel32 -luser32

%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $<

clean:
	rm -f $(OBJECTS1) $(TARGET1)
