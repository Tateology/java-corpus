
from java.lang import Thread
from java.util import Date
from java.util import Timer
from java.util import TimerTask


class MyTask(TimerTask):

    def __init__(self, message):
        self.message = message
    
    def run(self):
        print Date(self.scheduledExecutionTime()), self.message
        

# start a new timer 'daemon'
my_timer = Timer(1)

# add some tasks to the time queue
start_time = Date()
my_timer.schedule(MyTask("Python rules!"), start_time, 1000)
my_timer.schedule(MyTask("... and Java too :)"), start_time, 3000)

print "Start executing the tasks at", start_time
Thread.currentThread().sleep(20000)
