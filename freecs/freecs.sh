#!/bin/sh
#
# FreeCS startup script
# for RedHat
#
# chkconfig: 345 90 20
# description: FreeCS startup script
# processname: java
# pidfile: /var/run/freecs.pid

# Source function library.
. /etc/rc.d/init.d/functions

# set this to the directory where your JVM is installed
export JAVA_HOME=/usr/java/j2sdk1.4.1_02

# this sets the locales for java
export LANG="de_DE@euro:de_DE:de"

# setting environment for freecs
export PATH=$PATH:$JAVA_HOME/bin

# fill in the directory where you installed freecs
export FREECS_HOME=/usr/local/freecs
FREECS_BIN=java
FREECS_STARTUP_LOG=/var/log/freecs/freecs_startup.log
FREECS_PID=/var/run/freecs.pid
FREECS_USER=root

JARS=$CLASSPATH:$FREECS_HOME/lib/freecs.jar
JARS=$JARS:$FREECS_HOME/lib/xmlrpc-1.2-b1.jar

JARS=$JARS:$FREECS_HOME/lib/xmlrpc/commons-codec-1.4.jar
JARS=$JARS:$FREECS_HOME/lib/xmlrpc/commons-httpclient-3.1.jar

JARS=$JARS:$FREECS_HOME/lib/xmlrpc/xmlrpc-common-3.1.3.jar
JARS=$JARS:$FREECS_HOME/lib/xmlrpc/xmlrpc-client-3.1.3.jar
JARS=$JARS:$FREECS_HOME/lib/xmlrpc/xmlrpc-server-3.1.3.jar

JARS=$JARS:$FREECS_HOME/lib/xmlrpc/commons-logging-1.1.jar
JARS=$JARS:$FREECS_HOME/lib/xmlrpc/ws-commons-util-1.0.2.jar

# if sql-authentication is used, you will have to insert
# the path to your jdbc-driver here
# JARS=$JARS:$FREECS_HOME/lib/ext/mysql.jar
export CLASSPATH=$JARS
RETVAL=0

#like functions  from /etc/rc.d/init.d/functions
echo_failure() {
  [ "$BOOTUP" = "color" ] && $MOVE_TO_COL
  echo -n "["
  [ "$BOOTUP" = "color" ] && $SETCOLOR_FAILURE
  if [ -z "$1" ]; then
     echo -n "FAILED"
  else
     echo -n "$1"
  fi
  [ "$BOOTUP" = "color" ] && $SETCOLOR_NORMAL
  echo -n "]"
  echo -ne "\r"
  return 1
}


startfreecs() {
        gotbase=
        user=
        case $1 in
            '')    echo '$0: Usage: startfreecs [--user] {program}'
                   return 1;;
            --user)
                   shift
                   daemon_user=$1
                   shift
                   ;;
        esac

        # Save basename.
        [ -z $gotbase ] && base=`basename $1`

        # make sure it doesn't core dump anywhere;
        # while this could mask
        # problems with the daemon,
        # it also closes some security problems
        ulimit -c 0

        # Echo daemon
        [ "$BOOTUP" = "verbose" ] && echo -n " $base"

        # Set the maximum filedescriptors a little bit higher
        # on heavy usage, this may go up a little bit (every network 
        # connection is one filedescriptor)
        ulimit -n 8192

        # And start it up.
        if [ -z "$daemon_user" ]; then
           $* >$FREECS_STARTUP_LOG &
           hpid=$!
        else
           touch $FREECS_STARTUP_LOG
           su $daemon_user -c "$*" 2>/dev/null >$FREECS_STARTUP_LOG &
           hpid=$!
        fi
        /bin/ps h $hpid >/dev/null 2>&1 \
           && success "$base startup" || failure "$base startup"
        echo $hpid >$FREECS_PID

}

# See how we were called.
case "$1" in
  start)
        # check if freecs is already running - if not, start it
        echo -n "Starting FreeCS:"
        DATE=`date +%Y%m%d_%H%M_%S`
        cp /var/log/freecs/*.log /var/log/freecs/*.$DATE
        if [ ! -f /var/lock/subsys/freecs ]; then
            startfreecs --user $FREECS_USER $JAVA_HOME/bin/$FREECS_BIN -server -Xms128m -Xmx768m \
                 -cp $JARS freecs.Server -b=$FREECS_HOME

            RETVAL=$?
            [ $RETVAL -eq 0 ] && touch /var/lock/subsys/freecs
        else
            if [ -f $FREECS_PID ]; then
                echo_failure "ALREADY RUNNING"
            else
                echo_failure "DEAD"
            fi
        fi
        echo
        ;;
  stop)
        echo -n "Shutting down FreeCS:"
        if [ -f $FREECS_PID ] ; then
            pid=`head -1 $FREECS_PID`
            kill -SIGTERM $pid && echo_success
        else
            echo_failure "NOT RUNNING"
        fi
        rm -f $FREECS_PID
        rm -f /var/lock/subsys/freecs

        echo
        ;;
  status)
        pid=`pidof -o $$ -o $PPID -o %PPID -x $FREECS_BIN`
        if [ "$pid" != "" ] ; then
            echo "FreeCS is running ($pid)"
        else
            echo "FreeCS is stopped"
        fi
        RETVAL=$?
        ;;
  restart)
        $0 stop
        sleep 20
        $0 start
        ;;
  *)
        echo "Usage: $0 {start|stop|status|restart}"
        exit 1
esac

exit $RETVAL
