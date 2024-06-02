import mysql.connector as sql


def validate(dbname,email,password):


    mydb = sql.connect(
    host="localhost",
    user="root",
    password="Madhu_007",
    database=dbname
    )
    mycursor=mydb.cursor()
    mycursor.execute(f'''SELECT * from teachers where email="{email}" and password="{password}";''')

    myresult = mycursor.fetchall()
    mydb.close()
    for x in myresult:
        print(x)
    if len(myresult)!=0:
        return True
    return False


def checkdatabase(dbname):
    mydb = sql.connect(
        host="localhost",
        user="root",
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    mycursor.execute('SHOW DATABASES')
    myresult = mycursor.fetchall()
    mydb.close()
    for i in myresult:
        if dbname in i:
            return True
    return False

def createdatabse(dbname):
    mydb = sql.connect(
        host="localhost",
        user="root",
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    mycursor.execute('CREATE DATABASE {}'.format(dbname))
    mydb.close()




def checktable(dbname,tablename):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    mycursor.execute('show tables')
    myresult = mycursor.fetchall()
    mydb.close()
    print(tablename)
    for i in myresult:
        print(i)
        if tablename in i:
            print("true")
            return True
    return False

def createtable(dbname,tablename):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    mycursor.execute('CREATE table {} (rollno varchar(50),name varchar(50));'.format(tablename))
    mydb.close()

def createtable2(dbname,tablename):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    mycursor.execute('CREATE table {} (rollno varchar(50));'.format(tablename))
    mydb.close()


def checkstudent(dbname,tablename,rollno,nanme):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    mycursor.execute('select rollno from {} '.format(tablename))
    myresult = mycursor.fetchall()
    print(myresult)
    mydb.close()
    res=[]
    for i in myresult:
        res.append(str(i[0]))
    print('res',res)
    if rollno not in res:
        return False
    return True

def insertstudent(dbname,tablename,rollno,name):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    print(dbname,tablename,rollno,name)
    mycursor.execute('''insert into {}(rollno,name) VALUES ("{}","{}")'''.format(tablename, rollno,name))
    mydb.commit()

    print(mycursor.rowcount, "record inserted.")
    mydb.close()
    return True

def insertstudentbyteacher(dbname,tablename,rollno):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    print(dbname,tablename,rollno)
    mycursor.execute('''insert into {}(rollno) VALUES ("{}")'''.format(tablename, rollno))
    print('''insert into {}(rollno) VALUES ("{}")'''.format(tablename, rollno))
    mydb.commit()

    print(mycursor.rowcount, "record inserted.")
    mydb.close()
    return True

def insertCol(dbname,tablename,col):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    print('''alter table {} add column {} integer(1) default 0;'''.format(tablename,col))
    mycursor.execute('''ALTER TABLE {} ADD COLUMN '{}' INTEGER(1) DEFAULT 0;'''.format(tablename,col))
    mydb.commit()
    mydb.close()
    print("added")
def checkCols(dbname,tablename,col):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()

    mycursor.execute('''show columns from {};'''.format(tablename))
    myresult=mycursor.fetchall()
    print(myresult)
    if col not in myresult:
        insertCol(dbname,tablename,col)
    mydb.close()


def mark_attendance(dbname,tablename,rollno,col):
    mydb = sql.connect(
        host="localhost",
        user="root",
        database=dbname,
        password="Madhu_007",
    )
    mycursor = mydb.cursor()
    mycursor.execute('''update {} set {}=1 where rollno={} ;'''.format(tablename,col,rollno))
    mydb.commit()
    mydb.close()


