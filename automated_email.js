const axios = require("axios");
const nodemailer = require("nodemailer");
const mysql = require("mysql");

async function getTemperature() {
  const city = "Tokyo";
  const url = `https://wttr.in/${city}?format=%t`;

  try {
    const response = await axios.get(url);
    return response.data.trim();
  } catch (error) {
    console.error("Error fetching temperature");
    return null;
  }
}

async function sendEmail(temperature, recipient, recipientName) {
  const email = "my_email_address";
  const password = "my_gmail_auth";

  const transporter = nodemailer.createTransport({
    host: "smtp.gmail.com",
    port: 465,
    secure: true,
    auth: {
      user: email,
      pass: password,
    },
  });

  const mailOptions = {
    from: email,
    to: recipient,
    subject: "Automated Email",
    text: `Hi ${recipientName}, how are you? Today's temperature in Japan is ${temperature}. I will come to NY soon.`,
  };

  try {
    await transporter.sendMail(mailOptions);
    console.log("Email sent successfully!");
  } catch (error) {
    console.error(`Error sending email: ${error}`);
  }
}

function getEmailData() {
  return new Promise((resolve, reject) => {
    const connection = mysql.createConnection({
      host: "localhost",
      user: "my_sql_username",
      password: "my_sql_pass",
      database: "export_test_database",
    });

    connection.connect();

    connection.query("SELECT Users, `E-mail` FROM Email_database", (error, results, fields) => {
      connection.end();

      if (error) {
        reject(error);
      } else {
        resolve(results);
      }
    });
  });
}

async function job() {
  const temperature = await getTemperature();
  if (temperature !== null) {
    const emailData = await getEmailData();
    for (const row of emailData) {
      await sendEmail(temperature, row["E-mail"], row.Users);
    }
  }
}

// Call the job function without scheduling
job();
