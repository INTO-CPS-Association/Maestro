#from __future__ import print_function

import numpy as np
import matplotlib.pyplot as plt
import sys


def graph(filename):
#	converter = lambda x : print("'"+x+"'")
        converter = lambda x : 1 if x.strip() == "true" else 0
#1 if x=="false"  else 0 
	time , step , waterout , level1 , valve , valve2, level2= np.loadtxt(filename, delimiter=',', unpack=True, skiprows=1,converters={4:converter, 5:converter})
	
	print "Loaded file"
	fig = plt.figure()

	#print data
#	print valve

	#axl = fig.add_subplot(1,1,1,axisbg='white')
	plt.subplot(311)
	
	plot1=plt.plot(time , level1, label='level1')
	plt.ylim([-1,3])
	plt.xlim([0,12])
	
	#axl = fig.add_subplot(2,1,2,axisbg='white')
	plt.subplot(312)
	plot2=plt.plot(time , level2, label='level2')
	plt.ylim([0,4])
	plt.xlim([0,12])
	#plt.plot(time , level)

	plt.subplot(313)
	plot2=plt.plot(time , valve2, label='valve2')
	plt.ylim([0,1.5])
	plt.xlim([0,12])
	plt.title('FMU plot')
	
	plt.ylabel('Value')
	
	plt.xlabel('Data')
	
	#plt.legend([plot1,plot2])
	
	
	plt.legend(loc=5, borderaxespad=0., bbox_transform=plt.gcf().transFigure)
	#plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=5,
    #       ncol=1, mode="expand", borderaxespad=0.)
	plt.show()

graph(sys.argv[1])

#print 'Arguments are: ' , str(sys.argv)