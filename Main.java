import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static String userToken;
    private static boolean needsAuth;
    private static String httpMethod;

    public static void main(String[] args) {
        boolean exitObserver = false;

        Scanner user = new Scanner(System.in);
        String username;
        String password;
        String email = "";
        int option = 0;
        while (true) {
            System.out.println("Welcome." + "\n" + "Please select the number of your option:" +
                    "\n 1. Login" +
                    "\n 2. Register");
            do {
                try {
                    option = user.nextInt();
                } catch (Exception e) {
                    System.out.println("Remember to enter the number, not the the text");
                    user.nextLine();
                }
            } while (option > 2 || option < 1);
            System.out.println("Option #" + option + " selected");
            switch (option) {
                case 1:
                    System.out.println("EXIT? (if yes, write yes and press enter button)");
                    if (user.next().equals("yes")) {
                        exitObserver = false;
                        break;
                    }
                    do {
                        System.out.print("email:");
                        email = user.next();
                        System.out.print("password:");
                        password = user.next();
                        needsAuth = false;
                        httpMethod = "POST";
                    } while (!login(email, password));
                    exitObserver = true;
                    break;
                case 2:
                    System.out.println("EXIT?");
                    if (user.next().equals("yes")) {
                        exitObserver = false;
                        break;
                    }
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
                    exitObserver = true;
                    break;
            }
            if (exitObserver) {
                break;
            }
        }
        exitObserver = false;
        System.out.println("Hello, " + email);
        while (true) {
            System.out.println("\n please select one of the following options:" +
                    "\n 1. Create new queue" +
                    "\n 2. Delete queue" +
                    "\n 3. List existing queues" +
                    "\n 4. Push tasks" +
                    "\n 5. My user information" +
                    "\n 6. Exit" +
                    "\n if you want to exit from an option write option at the parameter");
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
                        if (queueName.equals("exit")) {
                            break;
                        }
                        needsAuth = true;
                        httpMethod = "POST";
                    } while (!createQueue(queueName));
                    break;
                case 2:
                    do {
                        System.out.print("Name of the queue to remove:");
                        queueName = user.next();
                        if (queueName.equals("exit")) {
                            break;
                        }
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
                        if (queueName.equals("exit")) {
                            break;
                        }
                        message = "";
                        Random rand = new Random();
                        String taskId = "[{\"TASK #\":\"" + rand.nextInt(100) + "\"},";
                        String emailSender = "{\"Email sender\":\"" + email + "\"},";
                        message = message.concat(taskId);
                        message = message.concat(emailSender);
                        System.out.println("Task/Message to push on the queue:(just 1 line, press enter and send a \".\")");
                        user.nextLine();
                        message = message.concat("[{\"Message:\":\"");
                        while (!user.nextLine().equals(".")) {
                            message = message.concat(user.nextLine());
                        }
                        message = message.concat("\"}]");
                        message = message.replace(" ", "+");
                        System.out.println(message);
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
            URL url = new URL("http://54.164.144.28/api/queue/create");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.out.print("Error at the creation of the queue:" + e);
            return false;
        }
        return true;
    }

    static boolean getUserInfo() {
        try {
            StringBuilder tokenUri = new StringBuilder("http://54.164.144.28/api/user-info");
            URL url = new URL(tokenUri.toString());
            sendGetRequest(url);
        } catch (Exception e) {
            System.out.println("Your information wasn't found -> Error:" + e);
            return false;
        }
        return true;
    }

    static boolean listQueues() {
        try {
            StringBuilder tokenUri = new StringBuilder("http://54.164.144.28/api/queue/list");
            URL url = new URL(tokenUri.toString());
            sendGetRequest(url);
        } catch (Exception e) {
            System.out.println("Error at listing existing queues:" + e);
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
            URL url = new URL("http://54.164.144.28/api/login");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.out.println("You made a mistake -> Error:" + e);
            return false;
        }
        return true;
    }

    static boolean pushTask(String message, String queueName) {
        try {
            String link = "http://54.164.144.28/api/queue/push?queue=" + queueName + "&body=" + message;
            URL url = new URL(link);
            sendPutRequest(url);
        } catch (Exception e) {
            System.out.println("Error at sending the task:" + e);
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
            URL url = new URL("http://54.164.144.28/api/register");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.out.println("Te enrollment fail ->Error:" + e);
            return false;
        }
        return true;
    }

    static boolean removeQueue(String name) {
        try {
            StringBuilder tokenUri = new StringBuilder("name=");
            tokenUri.append(URLEncoder.encode(name, "UTF-8"));
            URL url = new URL("http://54.164.144.28/api/queue/delete");
            sendRequest(url, tokenUri);
        } catch (Exception e) {
            System.out.println("Error at the removing of the queue:" + e);
            return false;
        }
        return true;
    }

    static void sendPutRequest(URL url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(httpMethod);
        con.setRequestProperty("Authorization", "Bearer " + userToken);
        BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = bf.readLine()) != null) {
            sb.append(line);
        }
        bf.close();
        System.out.println(sb.toString());
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
