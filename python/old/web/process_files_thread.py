from queue import Queue
from threading import Thread
from time import time
import os
import logging

from algorithms.trust import utils


class GetURLContentWorker(Thread):
   def __init__(self, queue):
       Thread.__init__(self)
       self.queue = queue

   def run(self):
       while True:
           directory, link = self.queue.get()
           utils.crawl2(link, directory, str(lines[0]), extract_html=False, save_printscreen=True)
           self.queue.task_done()

def main(f):
   ts = time()
   url_file = open(f, "r").readlines()
   urls = [l for l in url_file]
   queue = Queue()
   for x in range(8):
       worker = GetURLContentWorker(queue)
       worker.daemon = True
       worker.start()
   for u in urls:
       logger.info('Queueing {}'.format(u))
       queue.put((download_dir, link))
   # Causes the main thread to wait for the queue to finish processing all the tasks
   queue.join()
   print('Took {}'.format(time() - ts))