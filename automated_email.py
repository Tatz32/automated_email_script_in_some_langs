import smtplib
import requests
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import mysql.connector

def get_temperature():
    city = "Tokyo"
    url = f"https://wttr.in/{city}?format=%t"
    
    response = requests.get(url)
    if response.status_code == 200:
        temperature = response.text.strip()
        return temperature
    else:
        print("Error fetching temperature")
        return None

def send_email(temperature, recipient, recipient_name):
    email = "my_email_address"
    password = "auth_pass"

    msg = MIMEMultipart()
    msg["From"] = email
    msg["To"] = recipient
    msg["Subject"] = "Automated Email"

    body = f"Hi {recipient_name}, how are you? Today's temperature in Japan is {temperature}. I will come to NY soon."
    msg.attach(MIMEText(body, "plain"))

    try:
        server = smtplib.SMTP_SSL("smtp.gmail.com", 465)
        server.login(email, password)
        server.sendmail(email, recipient, msg.as_string())
        server.quit()
        print("Email sent successfully!")
    except Exception as e:
        print(f"Error sending email: {e}")

def get_email_data():
    connection = mysql.connector.connect(
        host="localhost",
        user="my_sql_username",
        password="my_sql_pass",
        database="export_test_database"
    )

    cursor = connection.cursor()
    cursor.execute("SELECT Users, `E-mail` FROM Email_database")
    email_data = cursor.fetchall()
    cursor.close()
    connection.close()

    return email_data

def job():
    temperature = get_temperature()
    if temperature is not None:
        email_data = get_email_data()
        for recipient_name, recipient_email in email_data:
            send_email(temperature, recipient_email, recipient_name)

# Call the job function without scheduling
job()
