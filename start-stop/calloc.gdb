python import threading, gdb

# Put a breakpoint in first instruction,
# and start the application
starti

# Stackcount needs to run for less than the overall time of this script,
# otherwise the process won't stop in time and no stacks will be recorded
shell ./stackcount.sh calloc c:calloc 4 &

# Sleep briefly to allow stackcount to be ready to trace
shell sleep 2

# Delay interrupting of the process to a time where
# stackcount has finished counting startup and has completed
python threading.Timer(8.0, lambda: gdb.post_event(lambda: gdb.execute("interrupt"))).start()

# Continue the process so that it can complete startup
cont
