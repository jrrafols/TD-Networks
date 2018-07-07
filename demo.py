from g import *
from math import exp
from random import *
from time import sleep

nodename = ['Leap\t','L-Leap\t','R-Leap\t','Leap-L-Leap','Leap-R-Leap','F\t','L\t','R\t','Wander\t']

def add(x,y):
    return x+y

class time:
    def __init__(self):
        self.timestep = 200000
        self.pointer = gdDrawTextCentered(window,'Time step = '+str(self.timestep),\
                                         ('Helvetica',20),135,540)
        self.del_list = list()
        gMakeVisible(window)
        
    def updateTime(self):
        gDelete(window,self.pointer)
        self.timestep += 1
        self.pointer = gdDrawTextCentered(window,'Time step = '+str(self.timestep),\
                                         ('Helvetica',20),135,540)

class world:
    def __init__(self, filename):
        file = open(filename+'.world' ,'r')
        self.env = [];
        self.pos = [0,0,0]
        
        #c0,c1 = 0,0;
        for line in file.readlines():
            temp = []
            for char in line:
                if char != '\n':
                    if char == '0' or char == '1' or\
                       char == '2' or char == '3' or\
                       char == '4' or char == '5' or\
                       char == '9':
                        temp.append(int(char))
                    elif char == 'A':
                        temp.append(0)
                        #self.pos[0],self.pos[1] = c0,c1
                #c1+=1
            
            self.env.append(temp)
            #c0+=1
            #c1 = 0
        file.close()
        
        #for i in self.env:
        #    print i
        #print self.pos
        
        self.x_len, self.y_len = len(self.env[0]),len(self.env)
        self.blocksize = 30
        
        self.window = Gwindow(gdViewport = (20,20,self.blocksize*self.x_len+20,self.blocksize*self.y_len+20))
        self.drawWorld()
        self.agent = None
    
    def loadPos(self):
        self.pos = []
        file = open(filename+'.pos' ,'r')
        line = file.readline()
        a = line.split(" ")
        for i in a:
            self.pos.append(int(i))
        file.close()
        self.agent = self.drawAgent()
    
    def drawWorld(self):
        for i in xrange(0,self.x_len+1):
            gdDrawLine(self.window, i*self.blocksize,0,i*self.blocksize,\
                       self.y_len*(self.blocksize),'grey')
        for i in xrange(0,self.y_len+1):
            gdDrawLine(self.window, 0, i*self.blocksize,\
                       self.x_len*(self.blocksize), i*self.blocksize, 'grey')
        
        for i in xrange(len(self.env)):
            for j in xrange(len(self.env[i])):
                if self.env[i][j] == 9: gdFillRectR(self.window,(j*self.blocksize)+1, \
                                                    (i*self.blocksize)+1,self.blocksize-3, \
                                                    self.blocksize-3,'grey') 
                elif self.env[i][j] == 1: gdFillRectR(self.window,(j*self.blocksize)+1, \
                                                    (i*self.blocksize)+1,self.blocksize-3, \
                                                    self.blocksize-3,'magenta')
                elif self.env[i][j] == 2: gdFillRectR(self.window,j*self.blocksize+1, \
                                                    i*self.blocksize+1,self.blocksize-3, \
                                                    self.blocksize-3,'yellow')
                elif self.env[i][j] == 3: gdFillRectR(self.window,j*self.blocksize+1,\
                                                    i*self.blocksize+1,self.blocksize-3, \
                                                    self.blocksize-3,'red')
                elif self.env[i][j] == 4: gdFillRectR(self.window,j*self.blocksize+1, \
                                                    i*self.blocksize+1,self.blocksize-3, \
                                                    self.blocksize-3,'blue')
                elif self.env[i][j] == 5: gdFillRectR(self.window,j*self.blocksize+1, \
                                                    i*self.blocksize+1,self.blocksize-3, \
                                                    self.blocksize-3,'green')

    def drawAgent(self):
        gDelete(self.window, self.agent)
        if self.pos[2] == 0:
            self.agent = gdDrawWedge(self.window, (self.pos[1]*self.blocksize)+(self.blocksize/2), \
                                  ((self.pos[0])*self.blocksize)+(self.blocksize/5), self.blocksize/1.5,250,40)
        elif self.pos[2] == 1:
            self.agent = gdDrawWedge(self.window, ((self.pos[1]+1)*self.blocksize)-(self.blocksize/5), \
                                  (self.pos[0]*self.blocksize)+(self.blocksize/2),self.blocksize/1.5,160,40)
        elif self.pos[2] == 2:
            self.agent = gdDrawWedge(self.window, (self.pos[1]*self.blocksize)+(self.blocksize/2), \
                                  ((self.pos[0]+1)*self.blocksize)-(self.blocksize/5),self.blocksize/1.5,70,40)
        elif self.pos[2] == 3:
            self.agent = gdDrawWedge(self.window, ((self.pos[1])*self.blocksize)+(self.blocksize/5), \
                                  (self.pos[0]*self.blocksize)+(self.blocksize/2),self.blocksize/1.5,-20,40)
        
        gMakeVisible(self.window)
        return self.agent
    
    def getObs(self):
        obs = None
        if self.pos[2] == 0:
            if self.pos[0] != 0: obs = self.env[self.pos[0]-1][self.pos[1]]
            else: obs = self.env[len(self.env) - 1][self.pos[1]]
        elif self.pos[2] == 1:
            if self.pos[1] != len(self.env[0]) - 1: obs = self.env[self.pos[0]][self.pos[1]+1]
            else: obs = self.env[self.pos[0]][0]
        elif self.pos[2] == 2:
            if self.pos[0] != len(self.env) - 1: obs = self.env[self.pos[0]+1][self.pos[1]]
            else: obs = self.env[0][self.pos[1]]
        elif self.pos[2] == 3:
            if self.pos[1] != 0: obs = self.env[self.pos[0]][self.pos[1]-1]
            else: obs = self.env[self.pos[0]][len(self.env[0])-1]
        return obs

    def getBitObs(self):
        temp = [0 for i in range(6)]
        temp[self.getObs()] = 1
        return temp
    
    def doAct(self, a):
        if a == 0 or a == 'F':
            if self.getObs() == 0:
                if self.pos[2] == 0:
                    if self.pos[0] != 0: self.pos[0] -= 1
                    else: self.pos[0] = len(self.env) - 1
                elif self.pos[2] == 1:
                    if self.pos[1] != len(self.env[0]) - 1: self.pos[1] += 1
                    else: self.pos[1] = 0
                elif self.pos[2] == 2:
                    if self.pos[0] != len(self.env) - 1: self.pos[0] += 1
                    else: self.pos[0] = 0
                elif self.pos[2] == 3:
                    if self.pos[1] != 0: self.pos[1] -= 1
                    else: self.pos[1] = len(self.env[0]) - 1
        elif a == 1 or a == 'R':
            self.pos[2] = (self.pos[2]+1)%4
        elif a == 2 or a == 'L':
            self.pos[2] = (self.pos[2]-1)%4

shiftx, shifty = -275,0
colors = ['Magenta','Yellow','Red','Blue','Green','White']
nodes = [(shiftx+515,shifty+280),(shiftx+400,shifty+390),(shiftx+630,shifty+390),(shiftx+400,shifty+530),(shiftx+630,shifty+530),\
         (shiftx+350,shifty+130),(shiftx+350,shifty+40),(shiftx+350,shifty+220),(shiftx+680,shifty+130)]
         
class TDNet:
    def __init__(self, filename):
        self.window = Gwindow(gdViewport = (600,20,1075,600))
        self.drawNets()
        self.del_list=[]
        
        
        #import weights here
        self.W = list()
        self.x = list()
        
        file = open(filename+'.state' ,'r')
        counter = 0
        for line in file.readlines():
            if counter == 0:
                start_act = int(line)
            elif counter == 1:
                #a = line.split(" ")
                for i in line.split(" "):
                    if i != '\n': self.x.append(float(i))
            else:
                temp = []
                #a = line.split(" ")
                for i in line.split(" "):
                    if i != '\n': temp.append(float(i))
                self.W.append(temp)
            counter+=1
        file.close()
        self.y_vals = self.Y(start_act)
             
   
    def sigma(self,y_val):
        return 1/(1+exp(-y_val))

    def X(self,a,o):
        self.x = [0.0 for i in range(156)]
        offset = a*(52)
        self.x[offset:offset+45] = self.y_vals
        self.x[offset+45+o[0]] = 1
        self.x[offset+45+6] = 1
        
    def dot(self,a,tempW):
        sum = 0.0
        for i in xrange(a*52,(a+1)*52):
            sum = sum + self.x[i]*tempW[i]
        return self.sigma(sum)

    def Y(self,a):
        y = [0.0 for i in range(45)]
        for i in xrange(45):
            y[i] = self.dot(a,self.W[i])
        return y
        
    def update(self,a,o):
        self.X(a,o)
        self.y_vals = self.Y(a)

    def printY(self):
        print '\t\tmagenta\tyellow\tred\tblue\tgreen'
        for i in xrange(9): print nodename[i], '\t', '%1.4f\t'% self.y_vals[(i*5)],\
                                  '%1.4f\t'% self.y_vals[(i*5)+1],\
                                  '%1.4f\t'% self.y_vals[(i*5)+2],\
                                  '%1.4f\t'% self.y_vals[(i*5)+3],\
                                  '%1.4f'% self.y_vals[(i*5)+4]
        print ' ' 
        
    def drawNets(self):
        gClear(self.window,'grey')
        gSetColor(self.window,gColorPen(self.window,xsize = 3))
    
        gSetColor(self.window,gColorPen(self.window,xsize = 2))
        gdDrawArrowheadR(self.window,shiftx+350,shifty+130,134,0,1,.11)#F->Bit
        gSetColor(self.window,gColorPen(self.window,xsize = 2))
        gdDrawArrowheadR(self.window,shiftx+350,shifty+40,134,64,1,.09)#L->Bit
        gdDrawArrowheadR(self.window,shiftx+350,shifty+220,134,-64,1,.09)#R->Bit
        
        gdDrawArrowheadR(self.window,shiftx+400,shifty+390,105,-80,1,.11)#L->Leap
        gdDrawArrowheadR(self.window,shiftx+630,shifty+390,-105,-80,1,.11)#R->Leap
        
        size = 16
        gdDrawTextCentered(self.window,'F',('Helvetica',size,'bold'),shiftx+420,shifty+122)
        gdDrawTextCentered(self.window,'L',('Helvetica',size,'bold'),shiftx+420,shifty+64)
        gdDrawTextCentered(self.window,'R',('Helvetica',size,'bold'),shiftx+420,shifty+176)
        gdDrawTextCentered(self.window,'Wander',('Helvetica',size,'bold'),shiftx+610,shifty+115)
        gdDrawTextCentered(self.window,'Leap',('Helvetica',size,'bold'),shiftx+545,shifty+215)
        gdDrawTextCentered(self.window,'Leap',('Helvetica',size,'bold'),shiftx+432,shifty+470)
        gdDrawTextCentered(self.window,'Leap',('Helvetica',size,'bold'),shiftx+662,shifty+470)
        gdDrawTextCentered(self.window,'L',('Helvetica',size,'bold'),shiftx+452,shifty+326)
        gdDrawTextCentered(self.window,'R',('Helvetica',size,'bold'),shiftx+578,shifty+326)
        
        gdOutlineRectR(self.window,shiftx+485,shifty+100,60,60)
        gdDrawCircle(self.window,shiftx+515,shifty+280,30)#Leap
        gdDrawCircle(self.window,shiftx+350,shifty+130,30)#F
        gdDrawCircle(self.window,shiftx+350,shifty+40,30)#L
        gdDrawCircle(self.window,shiftx+350,shifty+220,30)#R
        gdDrawCircle(self.window,shiftx+680,shifty+130,30)#Wander
        gdDrawCircle(self.window,shiftx+400,shifty+390,30)#L-Leap
        gdDrawCircle(self.window,shiftx+630,shifty+390,30)#R-Leap
        gdDrawCircle(self.window,shiftx+400,shifty+530,30)#Leap-L-Leap
        gdDrawCircle(self.window,shiftx+630,shifty+530,30)#Leap-R-Leap
        gSetColor(self.window,gColorPen(self.window,xsize = 6))
        gdDrawArrowheadR(self.window,shiftx+515,shifty+250,0,-88,1,.2)#Leap->Bit
        gdDrawArrowheadR(self.window,shiftx+400,shifty+500,0,-80,1,.2)#Leap->L
        gdDrawArrowheadR(self.window,shiftx+630,shifty+500,0,-80,1,.2)#Leap->R
        gdDrawArrowheadR(self.window,shiftx+650,shifty+130,-103,0,1,.17)#Wander->Bit
    
    def toRGB(self,color,x):
        if color == 'Red':
            return 1,(1-x),(1-x)
        elif color == 'Green':
            return (1-x),1,(1-x)
        elif color == 'Blue':
            return (1-x),(1-x),1
        elif color == 'Magenta':
            return 1,(1-x),1
        elif color == 'Yellow':
            return 1,1,(1-x)
        else:
            return 1,1,1
    
    def drawNode(self,node,numactive,activecolors,activations):
        to_delete = list()
        
        if numactive == 0:
            to_delete.append(gdDrawDisk(self.window,nodes[node][0],nodes[node][1],28,'white'))
        elif numactive == 1:
            r,g,b = self.toRGB(colors[activecolors[0]],activations[0])
            to_delete.append(gdDrawDisk(self.window,nodes[node][0],nodes[node][1],28,\
                       gColorRGB(self.window,r,g,b)))
        else:
            sum = reduce(add,activations)
            start = 90
            for i in xrange(numactive):
                pct = activations[i]/sum
                r,g,b = self.toRGB(colors[activecolors[i]],sum)
                to_delete.append(gdDrawWedge(self.window,nodes[node][0],nodes[node][1],28,start,360*pct,\
                      gColorRGB(self.window,r,g,b)))
                start = start + 360*pct
        return to_delete
            
    def updateNets(self, obs):
        gDelete(self.window,self.del_list)
        self.del_list = []
        temp = obs
        if temp == 0:
            r,g,b = self.toRGB(None,1)
        else:
            r,g,b = self.toRGB(colors[temp-1],1)
        self.del_list += [gdFillRectR(self.window,shiftx+486,shifty+102,57,57,gColorRGB(self.window,r,g,b))]
    
        b = list()
        for j in xrange(9):
            a = self.y_vals[j*5:(j+1)*5]
            for i in xrange(5):
                if a[i] < 0.01: a[i] = 0
                elif a[i] > 0.99: a[i] = 1
            b.append(a)
        
        for i in xrange(9):
            if b[i].count(1)==1:
                self.del_list += self.drawNode(i,1,[b[i].index(1)],[1])
            elif reduce(add,b[i])==0:
                self.del_list += self.drawNode(i,0,None,None)
            else:
                temp = b[i][:]
                temp.sort()
                temp.reverse()
                if temp[1] == 0:
                    self.del_list += self.drawNode(i,1,[b[i].index(temp[0])],[temp[0]])
                elif temp[2] == 0:
                    self.del_list += self.drawNode(i,2,[b[i].index(temp[0]),b[i].index(temp[1])],\
                             [temp[0],temp[1]])
                elif temp[3] == 0:
                    self.del_list += self.drawNode(i,3,[b[i].index(temp[0]),b[i].index(temp[1]),\
                                  b[i].index(temp[2])],[temp[0],temp[1],temp[2]])
                elif temp[4] == 0:
                    self.del_list += self.drawNode(i,4,[b[i].index(temp[0]),b[i].index(temp[1]),\
                                  b[i].index(temp[2]),b[i].index(temp[3])],\
                                 [temp[0],temp[1],temp[2],temp[3]])
                else:            
                    self.del_list += self.drawNode(i,5,[b[i].index(temp[0]),b[i].index(temp[1]),\
                                  b[i].index(temp[2]),b[i].index(temp[3]),\
                                  b[i].index(temp[4])],\
                                  [temp[0],temp[1],temp[2],temp[3],temp[4]])
             
        gdDrawTextCentered(self.window,'Obs',('Helvetica',14,'bold'),shiftx+515,shifty+130)
        gMakeVisible(self.window)

def step(a):
    global t
    w.doAct(a)
    w.drawAgent()
    t.update(a,[w.getObs()])
    t.updateNets(w.getObs())
    t.printY()


def F(): step(0)
def R(): step(1)
def L(): step(2)
def Q(): gQuit()
def Wander():
    for i in range(20):
        a = randint(0,3)
        if a == 3: a  = 0
        step(a)
        sleep(0.5)


def loadNet():
    global t
    w.loadPos()
    t = TDNet(filename)
    t.updateNets(w.getObs())
    gDelete(test,load_button)
    forward_Button = gdAddButton(test,'Step Forward',F,0,0)
    left_Button = gdAddButton(test,'Turn Left',L,0,30)
    right_Button = gdAddButton(test,'Turn Right',R,0,60)
    wander_Button = gdAddButton(test,'Wander',Wander,0,90)

####Read Info From 'config' file#####
file = open('config' ,'r')
filename = file.readline()
w = world(filename)
#####################################

test = Gwindow(gdViewport = (100,400,200,600))
load_button = gdAddButton(test,'Load',loadNet,0,0)
quit_Button = gdAddButton(test,'Quit',Q,0,150)

gMainLoop()