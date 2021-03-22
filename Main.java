import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;

public class Main {

    private static String userToken;
    private static boolean needsAuth;
    private static String httpMethod;

    public static void main(String[] args) {
        boolean exitObserver = false;
        // write your code here
        Scanner user = new Scanner(System.in);
        System.out.println("Welcome." + "\n" + "Please select the number of your option:" +
                "\n 1. Login" +
                "\n 2. Register" +
                "\n 3. Exit");
        int option = 0;
        do {
            try {
                option = user.nextInt();
            } catch (Exception e) {
                System.out.println("Remember to enter the number, not the the text");
                user.nextLine();
            }
        } while (option > 3 || option < 1);
        System.out.println("Option #" + option + " selected");
        String username;
        String password;
        String email = "";
        switch (option) {
            case 1:
                do {
                    System.out.print("email:");
                    email = user.next();
                    System.out.print("password:");
                    password = user.next();
                    needsAuth = false;
                    httpMethod = "POST";
                } while (!login(email, password));
                break;
            case 2:
                do {
                    System.out.print("username:");
                    username = user.next();
                    System.out.print("email:");
                    email = user.next();
                    System.out.print("password:");
                    password = user.next();
                    needsAuth = false;
                    httpMethod = "POST";
                } while (!register(username, email, password));
                break;
            case 3:
                System.out.println("good bye");
                break;
        }
        System.out.println("Hello, " + email);
        while (true) {
            System.out.println("\n please select one of the following options:" +
                    "\n 1. Create new queue" +
                    "\n 2. Delete queue" +
                    "\n 3. List existing queues" +
                    "\n 4. Push tasks" +
                    "\n 5. My user information" +
                    "\n 6. Exit");
            option = 0;
            do {
                try {
                    option = user.nextInt();
                } catch (Exception e) {
                    System.out.println("Remember to enter the number, not the text");
                    user.nextLine();
                }
            } while (option > 6 || option < 1);
            String queueName;
            String message;
            switch (option) {
                case 1:
                    do {
                        System.out.print("Name of the queue to create:");
                        queueName = user.next();
                        needsAuth = true;
                        httpMethod = "POST";
                    } while (!createQueue(queueName));
                    break;
                case 2:
                    do {
                        System.out.print("Name of the queue to remove:");
                        queueName = user.next();
                        needsAuth = true;
                        httpMethod = "DELETE";
                    } while (!removeQueue(queueName));
                    break;
                case 3:
                    do {
                        System.out.println("List of queues.");
                        needsAuth = true;
                        httpMethod = "GET";
                    } while (!listQueues());
                    break;
                case 4:
                    do {
                        System.out.println("Name of the queue:");
                        queueName = user.next();
                        System.out.println("Task/Message to push on the queue:");
                        user.nextLine();
                        message = user.nextLine();
                        needsAuth = true;
                        httpMethod = "PUT";
                    } while (!pushTask(message, queueName));
                    break;
                case 5:
                    do {
                        System.out.println("user info:");
                        needsAuth = true;
                        httpMethod = "GET";
                    } while (!getUserInfo());
                    break;
                case 6:
                    exitObserver = true;
                    break;
            }
            if (exitObserver) {
                break;
            }
        }
    }

    static boolean createQueue(String name) {
        try {
            StringBuilder tokenUri = new StringBuilder("name=");
            tokenUri.append(URLEncoder.encode(name, "UTF-8"));
            URL url = new URL("http://localhost:8000/api/queue/create");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.err.print("Error at the creation of the queue:" + e);
            return false;
        }
        return true;
    }

    static boolean getUserInfo() {
        try {
            StringBuilder tokenUri = new StringBuilder("http://localhost:8000/api/user-info");
            URL url = new URL(tokenUri.toString());
            sendGetRequest(url);
        } catch (Exception e) {
            System.err.println("Your information wasn't found -> Error:" + e);
            return false;
        }
        return true;
    }

    static boolean listQueues() {
        try {
            StringBuilder tokenUri = new StringBuilder("http://localhost:8000/api/queue/list");
            URL url = new URL(tokenUri.toString());
            sendGetRequest(url);
        } catch (Exception e) {
            System.err.println("Error at listing existing queues:" + e);
            return false;
        }
        return true;
    }

    static boolean login(String email, String password) {
        //This method is used to assembly the http request to login.
        try {
            //create the request with the info that will be send.
            StringBuilder tokenUri = new StringBuilder("email=");
            tokenUri.append(URLEncoder.encode(email, "UTF-8"));
            tokenUri.append("&password=");
            tokenUri.append(URLEncoder.encode(password, "UTF-8"));
            URL url = new URL("http://localhost:8000/api/login");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.err.println("You made a mistake -> Error:" + e);
            return false;
        }
        return true;
    }

    static boolean pushTask(String message, String queueName) {
        try {
            StringBuilder tokenUri = new StringBuilder("queue=");
            tokenUri.append(URLEncoder.encode(queueName, "UTF-8"));
            tokenUri.append("&body=");
            tokenUri.append(URLEncoder.encode(message, "UTF-8"));
            URL url = new URL("http://localhost:8000/api/queue/push");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.err.println("Error at sending the task:" + e);
            return false;
        }
        return true;
    }

    static boolean register(String username, String email, String password) {
        //This method is used to assembly the http request to register.
        try {
            //create the request with the info that will be send.
            StringBuilder tokenUri = new StringBuilder("name=");
            tokenUri.append(URLEncoder.encode(username, "UTF-8"));
            tokenUri.append("&email=");
            tokenUri.append(URLEncoder.encode(email, "UTF-8"));
            tokenUri.append("&password=");
            tokenUri.append(URLEncoder.encode(password, "UTF-8"));
            URL url = new URL("http://localhost:8000/api/register");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.err.print("Te enrollment fail ->Error:" + e);
            return false;
        }
        return true;
    }

    static boolean removeQueue(String name) {
        try {
            StringBuilder tokenUri = new StringBuilder("name=");
            tokenUri.append(URLEncoder.encode(name, "UTF-8"));
            URL url = new URL("http://localhost:8000/api/queue/delete");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.err.println("Error at the removing of the queue:" + e);
            return false;
        }
        return true;
    }

    static void sendGetRequest(URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (needsAuth && !userToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + userToken);
        }
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Charset", "UTF-8");

        System.out.println("\nSending GET request to URL : " + url);
        System.out.println("Response Code : " + connection.getResponseCode());
        System.out.println("Response Message : " + connection.getResponseMessage());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer response = new StringBuffer();

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        System.out.println(response.toString());
    }

    static void sendRequest(URL url, StringBuilder tokenUri) throws Exception {
        //configure the sending information
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (needsAuth && !userToken.isEmpty()) {
            connection.setRequestProperty("Authorization", "Bearer " + userToken);
        }
        connection.setRequestMethod(httpMethod);
        connection.setRequestProperty("Accept-Language", "UTF-8");
        connection.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
        outputStreamWriter.write(tokenUri.toString());
        outputStreamWriter.flush();
        //abstract of the request.
        System.out.println("\nSending " + httpMethod + " request to URL: " + url.toString());
        System.out.println("Post parameters: " + tokenUri.toString());
        System.out.println("Response CODE: " + connection.getResponseCode() + " ;" + connection.getResponseMessage());
        //print answer
        BufferedReader answer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String incomingLine;
        StringBuffer response = new StringBuffer();
        while ((incomingLine = answer.readLine()) != null) {
            response.append(incomingLine);
        }
        answer.close();
        System.out.println(response);
        if (!needsAuth && httpMethod.equals("POST")) {
            String[] splitResponse = response.toString().split(":");
            String answerArea = splitResponse[1];
            userToken = answerArea.substring(1, answerArea.indexOf("\"", 2));
            System.out.println(userToken);
        }
    }
}
