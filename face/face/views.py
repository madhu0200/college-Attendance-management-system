import datetime
import json

from django.http import HttpResponse
from django.shortcuts import render
import os
from django.http import JsonResponse
from rest_framework.response import Response
from rest_framework.decorators import api_view
import threading
import cv2
import pickle

import cv2
import mysql.connector as connection
from sqlalchemy import create_engine
import pandas as pd
import numpy as np
from django.http import JsonResponse
import numpy as np
from keras import models
from keras.models import Sequential
from keras.layers import Convolution2D
from keras.layers import MaxPool2D
from keras.layers import Flatten
from keras.layers import Dense
from .database import *
base_dir = os.getcwd()
base_dir = base_dir + "\\college\\testing\\"
face_detect = cv2.CascadeClassifier(base_dir+'cascade_algorithm\\haarcascade_frontalface_default.xml')

# Create your views here.
import base64
import os
def face(request):
    if request.method=='POST':
        print(request.POST.get('name'))
        base_dir = os.getcwd()
        base_dir = base_dir + "\\college\\training\\"
        #print(request.POST)
        classfolder=request.POST.get('classFolder')
        studentfolder=request.POST.get('studentfolder')
        department,year,section=classfolder.split('-')
        pinno=studentfolder.split('-')[0]
        name=request.POST.get('name')
        decodeit = open(base_dir+'{}-{}-{}\\{}\\{}'.format(department,year,section,pinno,name), 'wb')
        decodeit.write(base64.b64decode(request.POST.get('image')+ '=='))
        decodeit.close()
        return render(request, "hi.html")
    else:
        return render(request,"hi.html")

def login(request):
    if request.method=='POST':
        email=request.POST.get('email')
        password=request.POST.get('password')
        print(email, password)
        if validate("details",email,password):
           # print(email,password)
            response=HttpResponse("okkk")
            response.status_code=200
            return response
        response = HttpResponse("okk")
        response.status_code = 400
        return response

    else:
        return HttpResponse("okk")

def student_first_details(request):
    print(request.POST)
    base_dir=os.getcwd()
    base_dir=base_dir+"\\college\\training\\"
    print(base_dir)
    if request.method=='POST':
        department=request.POST.get('department')
        year=request.POST.get('year')
        section=request.POST.get('section')

        dbname="details"
        print(dbname)
        if not checkdatabase(dbname):
            createdatabse(dbname)

        tablename = "{}{}{}".format(department.lower(), year, section.lower())
        if not checktable(dbname, tablename):
            createtable(dbname, tablename)


        dbname=dbname=request.POST.get("dbname").split('.')[0]
        print(dbname)
        if not checkdatabase(dbname):
            createdatabse(dbname)
        tablename="{}{}{}".format(department.lower(), year, section.lower())
        if not checktable(dbname,tablename):
            createtable2(dbname,tablename)

        print(base_dir+"{}-{}-{}".format(department,year,section))
        if not os.path.exists(base_dir+"{}-{}-{}".format(department,year,section)):
            os.mkdir(base_dir+"{}-{}-{}".format(department,year,section))
        print(department,year,section)

        response = HttpResponse("ok")
        response.status_code = 200
        return response

    else:
        return HttpResponse("ok")


def studentseconddetails(request):
    base_dir = os.getcwd()
    base_dir = base_dir + "\\college\\training\\"
    if request.method=='POST':
        department = request.POST.get('department')
        year = request.POST.get('year')
        section = request.POST.get('section')
        pinno = request.POST.get('pinno')
        name = request.POST.get('name')

        dbname="details"

        print(dbname)
        if not checkdatabase(dbname):
            createdatabse(dbname)
        tablename = "{}{}{}".format(department.lower(), year, section.lower())
        if not checktable(dbname, tablename):
            createtable(dbname, tablename)
        if checkstudent(dbname,tablename,pinno,name):
            insertstudent(dbname,tablename,pinno,name)
        dbname =request.POST.get("dbname").split('.')[0]
        print(dbname)
        if not checkdatabase(dbname):
            createdatabse(dbname)
        tablename = "{}{}{}".format(department.lower(), year, section.lower())
        if not checktable(dbname, tablename):
            createtable(dbname, tablename)

        print(department,year,section,pinno,name)
        if not os.path.exists(base_dir+"{}-{}-{}\\{}".format(department,year,section,pinno)):
            os.mkdir(base_dir+"{}-{}-{}\\{}".format(department,year,section,pinno))
        else:
            files=os.listdir(base_dir+"{}-{}-{}\\{}\\".format(department,year,section,pinno))
            if len(files)==0:
                response = HttpResponse("ok")
                response.status_code = 200
                return response
            else:
                response = HttpResponse("face is already trained")
                response.status_code = 400
                response.message="face is already trained"
                return response
    else:
        return HttpResponse("ok")

def createClass(request):
    base_dir = os.getcwd()
    base_dir = base_dir + "\\college\\training\\"
    if request.method=='POST':
        folder=request.POST.get('classFolder')
        print(folder)

        TrainingImagePath=base_dir+folder
        for student in os.listdir(TrainingImagePath):
            if len([file for file in os.listdir(TrainingImagePath+'\\'+student)])==0:
                response = HttpResponse("face is already trained")
                response.status_code = 400
                return response



        t1=model(TrainingImagePath,request.POST.get('classFolder'))
        response = HttpResponse("face is already trained")
        response.status_code = 200
        return response





def model(TrainingImagePath,classFolder):
    try:
        base_dir = os.getcwd()
        base_dir = base_dir + "\\college\\training\\"
        from keras.preprocessing.image import ImageDataGenerator

        train_datagen = ImageDataGenerator(
            shear_range=0.1,
            zoom_range=0.5,
            horizontal_flip=True)

        # Defining pre-processing transformations on raw images of testing data
        # No transformations are done on the testing images
        test_datagen = ImageDataGenerator()
        # Generating the Training Data
        training_set = train_datagen.flow_from_directory(
            TrainingImagePath,
            target_size=(128, 128),
            batch_size=5,
            class_mode='categorical')

        # Generating the Testing Data
        test_set = test_datagen.flow_from_directory(
            TrainingImagePath,
            target_size=(128, 128),
            batch_size=5,
            class_mode='categorical')

        # Printing class labels for each face
        test_set.class_indices

        '''############ Creating lookup table for all faces ############'''
        # class_indices have the numeric tag for each face
        TrainClasses = training_set.class_indices

        if len(TrainClasses) != 0:
            total_rows = len(TrainClasses) * 50

            # Storing the face and the numeric tag for future reference
            ResultMap = {}
            for faceValue, faceName in zip(TrainClasses.values(), TrainClasses.keys()):
                ResultMap[faceValue] = faceName

            # Saving the face map for future reference
            import pickle
            with open(base_dir + "face_datasets\\{}.pkl".format(classFolder), 'wb') as fileWriteStream:
                pickle.dump(ResultMap, fileWriteStream)

            # The model will give answer as a numeric tag
            # This mapping will help to get the corresponding face name for it
            print("Mapping of Face and its ID", ResultMap)

            # The number of neurons for the output layer is equal to the number of faces
            OutputNeurons = len(ResultMap)
            print('\n The Number of output neurons: ', OutputNeurons)

            '''######################## Create CNN deep learning model ########################'''


            '''Initializing the Convolutional Neural Network'''
            classifier = Sequential()

            ''' STEP--1 Convolution
            # Adding the first layer of CNN
            # we are using the format (64,64,3) because we are using TensorFlow backend
            # It means 3 matrix of size (64X64) pixels representing Red, Green and Blue components of pixels
            '''
            classifier.add(
                Convolution2D(128, kernel_size=(5, 5), strides=(1, 1), input_shape=(128, 128, 3), activation='relu'))

            '''# STEP--2 MAX Pooling'''
            classifier.add(MaxPool2D(pool_size=(2, 2)))

            '''############## ADDITIONAL LAYER of CONVOLUTION for better accuracy #################'''
            classifier.add(Convolution2D(64, kernel_size=(5, 5), strides=(1, 1), activation='relu'))

            classifier.add(MaxPool2D(pool_size=(2, 2)))
            '''############## ADDITIONAL LAYER of CONVOLUTION for better accuracy #################'''
            classifier.add(Convolution2D(32, kernel_size=(5, 5), strides=(1, 1), activation='relu'))

            classifier.add(MaxPool2D(pool_size=(2, 2)))

            '''# STEP--3 FLattening'''
            classifier.add(Flatten())

            '''# STEP--4 Fully Connected Neural Network'''
            classifier.add(Dense(64, activation='relu'))

            classifier.add(Dense(OutputNeurons, activation='softmax'))

            '''# Compiling the CNN'''
            # classifier.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
            classifier.compile(loss='categorical_crossentropy', optimizer='adam', metrics=["accuracy"])

            ###########################################################
            import time
            # Measuring the time taken by the model to train
            StartTime = time.time()

            # Starting the model training
            classifier.fit(
                training_set,
                steps_per_epoch=10,
                epochs=total_rows // 10,
                validation_data=test_set,
                validation_steps=10)
            EndTime = time.time()
            classifier.save(base_dir + "\\models_pkl\\{}.h5".format(classFolder),
                            include_optimizer=True)
            print("###### Total Time Taken: ", EndTime - StartTime, 'Minutes ######')
            response = HttpResponse("ok")
            response.status_code = 200
            return response
        else:
            response = HttpResponse("not possible to create the class")
            response.status_code = 400
            return response
    except:
        response = HttpResponse("not possible to create the class")
        response.status_code = 400
        return response



@api_view(['GET', 'POST'])

def test(request):
    if request.method=="POST":
        #print(request.POST)
        base_dir = os.getcwd()
        base_dir = base_dir + "\\college\\testing\\"
        # print(request.POST)
        classfolder = request.POST.get('classFolder')

        department, year, section = classfolder.split('-')

        name = request.POST.get('name')
        decodeit = open(base_dir + '{}'.format( name), 'wb')
        decodeit.write(base64.b64decode(request.POST.get('image') + '=='))
        decodeit.close()

        import pandas as pd




        video = cv2.VideoCapture(0)

        try:
            print('fetching details')
            base_dir = os.getcwd()
            print(base_dir)
            base_dir = base_dir + "\\college\\training\\"
           # base_dir = base_dir + "\\college\\training\\"
            print(base_dir)
            print(base_dir+'face_datasets\\{}-{}-{}.pkl'.format(department, year, section))
            print(base_dir+"models_pkl\\{}-{}-{}.h5".format(department, year, section))
            file = pickle.load(open(base_dir+'face_datasets\\{}-{}-{}.pkl'.format(department, year, section), 'rb'))
            print('read')
            model = models.load_model(base_dir+"models_pkl\\{}-{}-{}.h5".format(department, year, section))
            print('read')

            img=cv2.imread(os.getcwd()+"\\college\\testing\\{}".format(request.POST.get('name')))
            print(os.getcwd()+"\\college\\testing\\{}".format(request.POST.get('name')))
            face = face_detect.detectMultiScale(img, 1.3, 5)
            i=0
            global faces
            faces={}
                # print(len(face))
            dbname = dbname = request.POST.get("dbname").split('.')[0]
            print(dbname)
            if not checkdatabase(dbname):
                createdatabse(dbname)
            tablename = "{}{}{}".format(department.lower(), year, section.lower())
            if not checktable(dbname, tablename):
                createtable2(dbname, tablename)
            classfolder = request.POST.get('classFolder')
            #studentfolder = request.POST.get('studentfolder')

            department, year, section = classfolder.split('-')

            print(department)


            for (x, y, w, h) in face:
                print("hi")
                real_img = img[y:y + h, x:x + w]
                img = cv2.resize(real_img, (128, 128))
                img = np.array(img).reshape(-1, 128, 128, 3)
                name = model.predict(img)
                print(file[np.argmax(name)])
                faces[i]=file[np.argmax(name)]

                date = datetime.datetime.now()
                date = str(date.year) + "_" + str(date.month) + "_" + str(date.day)
                try:
                    checkCols(dbname, tablename, date)
                except:
                    print("hi")
                try:
                    print(dbname,tablename)
                    if not checkstudent(dbname, tablename, faces[i], "madhu"):
                        insertstudentbyteacher(dbname, tablename, faces[i])
                except:
                    print("hi")
                mark_attendance(dbname,tablename,faces[i],date)
          #  os.remove(os.getcwd() + "\\college\\testing\\{}".format(request.POST.get('name')))
            return Response({"message":faces},status=200)

        except Exception as e:
            print(type(e))
    else:
        return Response({"message": faces}, status=200)
    os.remove(os.getcwd() + "\\college\\testing\\{}".format(request.POST.get('name')))
    return Response({"message": faces}, status=200)

def json_serializable(obj):
    if isinstance(obj, np.ndarray):
        return obj.tolist()
    return obj

def generateattendance(request):
    global data
    if request.method=="POST":
        # Step 1: Create a DataFrame with the data
        dbname=request.POST.get('dbname').split('.')[0]
        tablename=request.POST.get("department")+request.POST.get("year")+request.POST.get("section")
        tablename=tablename.lower()

        db_url = "mysql+mysqlconnector://{USER}:{PWD}@{HOST}/{DBNAME}"
        # Replace the values below with your own
        # DB username, password, host and database name
        db_url = db_url.format(
            USER="root",
            PWD="Madhu_007",
            HOST="localhost:3306",
            DBNAME=dbname
        )
        engine = create_engine(db_url)

        with engine.begin() as conn:
            # Load all the rows from the table
            # 'largest_cities' into pandas DataFrame
            attendence = pd.read_sql_table(
                table_name=tablename,
                con=conn,
                # Set the column 'City' as the index

            )
        # print(attendence)
        cols = attendence.columns
        rolls = attendence.iloc[:, 0]
        total = len(cols) - 1
        attendence_Sheet = np.array(attendence.iloc[:, 1:])
        # print(attendence_Sheet)
        # print([total]*attendence.shape[0],attendence_Sheet.sum(axis=1),attendence_Sheet.sum(axis=1)/total)
        array = np.array([rolls, [total] * attendence.shape[0], attendence_Sheet.sum(axis=1),
                          (attendence_Sheet.sum(axis=1) / total) * 100])
        array=json.dumps({'rolls':array[0],"total":array[1],"no_of_classes_present":array[2],"percentage":array[3]},default=json_serializable)
        data=array
        print("get attendance",data)
        return HttpResponse({"message": array}, status=200,content_type="application/json")
    else:
        print("get attendance",data)
        return JsonResponse({"nums": data}, status=200, content_type="application/json")





