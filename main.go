package main

import (
	"bytes"
	"crypto/tls"
	"database/sql"
	"fmt"
	"log"
	"net/http"
	"net/smtp"
	"strings"

	_ "github.com/go-sql-driver/mysql"
	"github.com/jordan-wright/email"
)

func getTemperature() (string, error) {
	city := "Tokyo"
	url := fmt.Sprintf("https://wttr.in/%s?format=%%t", city)

	resp, err := http.Get(url)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != 200 {
		return "", fmt.Errorf("Error fetching temperature")
	}

	buf := new(bytes.Buffer)
	buf.ReadFrom(resp.Body)
	temperature := strings.TrimSpace(buf.String())

	return temperature, nil
}

func sendEmail(temperature, recipient, recipientName string) error {
	from := "my_email_address"
	password := "my_auth_password"
	to := recipient

	e := email.NewEmail()
	e.From = from
	e.To = []string{to}
	e.Subject = "Automated Email"
	e.Text = []byte(fmt.Sprintf("Hi %s, how are you? Today's temperature in Japan is %s. I will come to NY soon.", recipientName, temperature))

	auth := smtp.PlainAuth("", from, password, "smtp.gmail.com")

	// Use smtp.SendMail with port 587 and smtp.StartTLSClientConfig
	err := e.SendWithStartTLS("smtp.gmail.com:587", auth, &tls.Config{ServerName: "smtp.gmail.com", InsecureSkipVerify: true})
	if err != nil {
		return err
	}
	fmt.Println("Email sent successfully!")
	return nil
}

func getEmailData() ([][2]string, error) {
	db, err := sql.Open("mysql", "root:my_sql_pass@tcp(localhost:3306)/export_test_database")
	if err != nil {
		return nil, err
	}
	defer db.Close()

	rows, err := db.Query("SELECT Users, `E-mail` FROM Email_database")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var emailData [][2]string
	for rows.Next() {
		var name, email string
		err := rows.Scan(&name, &email)
		if err != nil {
			return nil, err
		}
		emailData = append(emailData, [2]string{name, email})
	}

	return emailData, nil
}

func job() {
	temperature, err := getTemperature()
	if err != nil {
		log.Printf("Error getting temperature: %v\n", err)
		return
	}

	emailData, err := getEmailData()
	if err != nil {
		log.Printf("Error getting email data: %v\n", err)
		return
	}

	for _, recipient := range emailData {
		name := recipient[0]
		email := recipient[1]
		err := sendEmail(temperature, email, name)
		if err != nil {
			log.Printf("Error sending email to %s: %v\n", email, err)
		}
	}
}

func main() {
	job()
}
