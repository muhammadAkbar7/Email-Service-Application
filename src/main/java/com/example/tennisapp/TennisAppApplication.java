package com.example.tennisapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.*;
import java.io.IOException;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping; // added
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController; // added
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@RestController // added
public class TennisAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(TennisAppApplication.class, args);
	}

	@GetMapping("/hello") // added
	public String hello() { // added
		return "Hello!";
	} // added

	@PostMapping("/notify-me")
	public String notifyMe(@RequestBody Map<String, String> body) throws IOException, ExecutionException, InterruptedException {
		Firestore db  = getFirestore();
		String email = body.get("email");
		DocumentReference document = db.collection("emails").document(email); // collection = place we are saving it to, sets the document to get only one email
		ApiFuture<WriteResult> data = document.set(body);
		System.out.println(data.get().getUpdateTime()); // gets the time the record is updated

		sendEmail(email);
		return new ObjectMapper().writeValueAsString(body);
	}

	private void sendEmail(String email) throws IOException { // got it from the sendgrid api website
		Email from = new Email("sampleemail425@gmail.com");
		String subject = "Thanks for signing up for TennisX!";
		Email to = new Email(email); // send to the emails we are passing in (coming from form)
		Content content = new Content("text/plain", "Welcome! Get ready to have more fun playing tennis!");
		Mail mail = new Mail(from, subject, to, content);

		SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));
		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			System.out.println(response.getStatusCode());
			System.out.println(response.getBody());
			System.out.println(response.getHeaders());
		} catch (IOException ex) {
			throw ex;
		}
	}

	private Firestore getFirestore() throws IOException {
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
		FirebaseOptions options = new FirebaseOptions.Builder()
				.setCredentials(credentials)
				.setProjectId("corded-actor-379300")
				.build();
		FirebaseApp.initializeApp(options);

		Firestore db = FirestoreClient.getFirestore();
		return db;
	}
}
