
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class httpc {
	private static String host = "";
    private static String url = "";
    private static String path = "";
    protected static String method = "";
    protected static boolean verbose;
    private static String inline = "";
    static List<String> headerList = new ArrayList<String>();
    private static Socket client = null;
    private static StringBuilder fileData = null;

	public static void main(String[] args) throws Exception {
		
    for(;;) {   
            System.out.print("\nEnter Command: ");
            Scanner sc = new Scanner(System.in);
            String request = sc.nextLine();
            ArrayList<String> clientRequest = new ArrayList<>();
            
            if(request.isEmpty()) {
            	System.out.println("Please enter a Command...");
            }else {
            	String[] arr = request.split(" ");
         	    for (int i = 0; i < arr.length; i++) {
         	        clientRequest.add(arr[i]);
         	    }
         	  
                if(clientRequest.size() == 1 || !clientRequest.get(0).contains("httpc")){
                    System.out.println("Invalid Command. Please provide valid httpc command!");
    	         }
     
                // help command functionality
              else if (clientRequest.contains("help")) {

                   if(clientRequest.contains("get")){
                       System.out.println("\nusage: httpc get [-v] [-h key:value] URL\nGet executes a HTTP GET request for a given URL.\n-v Prints the detail of the response such as protocol, status, and headers.\n-h key:value Associates headers to HTTP Request with the format 'key:value'.");
                   }
                   else if(clientRequest.contains("post")){
                       System.out.println("\nusage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\nPost executes a HTTP POST request for a given URL with inline data or from file.\n-v Prints the detail of the response such as protocol, status, and headers.\n-h key:value Associates headers to HTTP Request with the format 'key:value'.\n-d string Associates an inline data to the body HTTP POST request.\n-f file Associates the content of a file to the body HTTP POST request.\n\nEither [-d] or [-f] can be used but not both.");
                   }
                   else{
                       System.out.println("\nhttpc is a curl-like application but supports HTTP protocol only.\nUsage:\n  httpc command [arguments]\nThe commands are:\n  get executes a HTTP GET request and prints the response.\n  post executes a HTTP POST request and prints the response.\n  help prints this screen.\n\nUse \"httpc help [command]\" for more information about a command.");
                   }		
    	}
              else {
            	  // main user command implementation
                  sendRequest(clientRequest);
                  
                  BufferedReader brr = new BufferedReader(new InputStreamReader(client.getInputStream()));
                  String statusCode = brr.readLine();
                  
                  if(clientRequest.contains("-o")){
                	  // print response to given file   ***************TODO
                  }else {
    					// print response in console
    					printResponse(brr, statusCode);
    				}

                  brr.close();
    				
                  client.close();
                 } 	    
         	    
			}
            }
 }

	private static void printResponse(BufferedReader brr, String statusCode) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("\nOutput:\n");
        String st;
        if(verbose = true){
            System.out.println(statusCode);
        while((st = brr.readLine()) !=null ) {
            System.out.println(st);
            if(st.equals("}"))
                break;
            }
        }
        else{
        	boolean flag = false;
            while((st = brr.readLine()) !=null) {
                if(st.trim().equals("{")) 
                flag = true;
                if(flag){
                System.out.println(st);
                if(st.equals("}"))
                    break;
                }
            }
            }
	}



	private static void sendRequest(ArrayList<String> clientRequest) throws URISyntaxException, UnknownHostException, IOException{
		// TODO Auto-generated method stub
		
		if (clientRequest.get(1).contains("get") && (clientRequest.contains("-d") || clientRequest.contains("-f"))) {
			System.out.println("[-d] or [-f] are not allowed for GET Request");
		}
		else if (clientRequest.get(1).contains("post") && (clientRequest.contains("-d")) && clientRequest.contains("-f")) {
			System.out.println("Either [-d] or [-f] can be used but not both.");
		}
		
		//getting host, URL and other details
		//parsing user input
		method = clientRequest.get(1).equals("get") ? "GET" : "POST";
		
		for (int i = 2; i < clientRequest.size(); i++) {
			if (clientRequest.get(i).startsWith("http://") || clientRequest.get(i).startsWith("https://")) {     // deal with inverted commas here *******
				url = clientRequest.get(i);
		}else if (clientRequest.get(i).equals("-v")) {
			verbose = true;
		}else if (clientRequest.get(i).equals("-h")) {

			headerList.add(clientRequest.get(i + 1));

		} else if (clientRequest.get(i).equals("-d")) {

			inline = clientRequest.get(i + 1);

		} else if (clientRequest.get(i).equals("-f")) {

			path = clientRequest.get(i + 1);
		}
	}
		
		
		URI uri = new URI(url);
	    host = uri.getHost();

		//establish connection
		client = new Socket(host, 80);
		OutputStream output = client.getOutputStream();
		
		//getting query parameters
		String path1 = uri.getPath();
		String query = uri.getQuery();

		if (path1 != null && query != null) {
			if (query.length() > 0 || path1.length() > 0) {
				path1 = path1 + "?" + query;
			}
		}

		PrintWriter writer = new PrintWriter(output);
        // Add method, parameters to request
		if (path1.length() == 0) {
			writer.println(method + " / HTTP/1.0");
		} else {
			writer.println(method + " " + path1 + " HTTP/1.0");
		}
        // Add host to request
		writer.print("Host: "+host+"\r\n");
		
		// for inline data (-d)
	    if(clientRequest.contains("-d")) {
	        inline = clientRequest.get(clientRequest.indexOf("-d")+1);
	        if(inline.contains("\'")){
	        inline = inline.replace("\'", "");
	        }
	        writer.print("Content-Length: "+ inline.length()+"\r\n");
	    }
	    // for sending file data in request (-f)
	    else if(clientRequest.contains("-f")){
	        File file = new File(path);
	        
	        BufferedReader br = new BufferedReader(new FileReader(file)); 
	        String string = br.readLine();
	        while (string != null){
	            fileData.append(string);
	        }
	        writer.println("Content-Length: "+ fileData.length()+"\r\n");
	        br.close();
	    }
	    
	    // adding headers to request (-h)
		if (clientRequest.contains("-h")) {
			if (!headerList.isEmpty()) {
				for (int i=0; i<headerList.size(); i++) {
					String[] headerKeyValue = headerList.get(i).split(":");
					writer.write(headerKeyValue[0] + ":" + headerKeyValue[1] +"\r\n");
				}
			}
		}

		if (clientRequest.contains("-d")) {
			writer.print("\r\n"+ inline+ "\r\n");
		} else if (clientRequest.contains("-f")) {
			writer.print("\r\n"+ fileData.toString()+ "\r\n"); 
		} else {
			writer.print("\r\n");
		}

		writer.flush();	    
		
	}
}