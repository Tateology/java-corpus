MAKEFILE = $(shell echo gmake)
MAKEFILE:sh = echo make
ROOT_DIR = $(shell cd ..; pwd)
ROOT_DIR:sh = pwd
OS = $(shell uname -s)
OS:sh = uname -s

CONF=$(OS)-x86

PNAME=fs_server_test
CC=gcc

SOURCES=\
	$(ROOT_DIR)/src/queue.c \
	$(ROOT_DIR)/src/blocking_queue.c \
	$(ROOT_DIR)/src/util.c \
	$(ROOT_DIR)/src/array.c \
	$(ROOT_DIR)/src/dirtab.c \
	$(ROOT_DIR)/test/test_main.c

SRC_DIRS=\
	$(ROOT_DIR)/src $(ROOT_DIR)/test

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/dist/$(CONF)
INSTALL_DIR=../../release/bin/nativeexecution/$(CONF)
LINE=sh $(ROOT_DIR)/make/line

EXEC=$(DIST_DIR)/$(PNAME)


#CF_COMMON         = --std=c99 -s -O2
CF_COMMON         = --std=c99 -g -Wall -DTEST
CF_Linux-x86      = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m32
CF_Linux-x86_64   = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_MacOSX-x86     = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m32
CF_MacOSX-x86_64  = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m64
CF_SunOS-sparc    = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -m32
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -m32

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc
LF_Linux-x86      = $(LF_COMMON) -lpthread
LF_Linux-x86_64   = $(LF_COMMON) -lpthread

CFLAGS=$(CF_$(CONF))
LDFLAGS=$(LF_$(CONF))

all: $(EXEC)
	@$(LINE) =
	@echo $(EXEC); echo
	@ls -la $(EXEC) | sed "s#$(EXEC)##"
	@FILE=`(ls /bin/file || ls /usr/bin/file) 2>/dev/null`; \
	   if [ -n "$${FILE}" ]; then $${FILE} ${EXEC} | sed "s#$(EXEC):.##"; fi
	@$(LINE) =

include $(ROOT_DIR)/make/Makefile.$(MAKEFILE)

$(EXEC): $(DIST_DIR) $(OBJS)
	@$(LINE) -
	$(LINK.c) -o $@ $(OBJS)

$(OBJ_DIRS) $(DIST_DIR):
	$(MKDIR) $@

clean: clean_deps
	$(RM) -r $(OBJ_DIR) $(DIST_DIR)

clean-all:
	$(RM) -r $(ROOT_DIR)/build
	$(RM) -r $(ROOT_DIR)/dist
	$(RM) .make.state*

install:
	cp $(EXEC) $(INSTALL_DIR)

