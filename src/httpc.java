//-----------------------------------------------------
//Lab Assignment 1 
//© Jasmine Kaur, Sai Sukruth Nimmala
//Written by: (40103309) & (40125068)
//-----------------------------------------------------

import java.io.*;
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
    protected static String method = "";
    private static StringBuilder fileData = null;
    private static Socket client = null;
    private static String newLocation = "";

	public static void main(String[] args) throws Exception {
		 boolean redirect = false;
		 int count = 0;
    for(;;) {  
    	String request; 
    	
    	if(redirect && count<1) {             // if(true) - execute
    		count++;
            System.out.println("\nRedirection done.\n");
            System.out.println("\nRedirect to \"" + newLocation + "\"");
    		request = "httpc get -v " + newLocation ;
    		//redirect = false;
    	}	
    	else {
    		System.out.print("\nEnter Command: ");
            Scanner sc = new Scanner(System.in);   
            request = sc.nextLine();	
    	}
    	
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
            	  try {
            		  
                  boolean verbose = sendRequest(clientRequest, redirect);
                

          	      client.close();
                  
                  
                  
            	  } catch (Exception e) {
            		  System.out.println("Invalid URL. Please provide valid httpc get or httpc post URL!");
            		  System.out.println(e);  
      				continue;
      			}
             } 	    
         	    
			}
            }
 }

	private static void printResponse(BufferedReader brr, String statusCode, boolean verbose) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("\nOutput:\n");
		//System.out.println(verbose);
        String st;
        if(verbose == true){
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



	private static boolean sendRequest(ArrayList<String> clientRequest, boolean redirect) throws URISyntaxException, UnknownHostException, IOException{
		// TODO Auto-generated method stub
		boolean verbose = false;
		String inline = "";
		String path = "";
		List<String> headerList = new ArrayList<String>();
		
		
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
			if (clientRequest.get(i).startsWith("\'http://") || clientRequest.get(i).startsWith("\'https://") || clientRequest.get(i).startsWith("http://") || clientRequest.get(i).startsWith("https://")) {    
				url = clientRequest.get(i);
			    url = url.replace("\'", "");
		}else if (clientRequest.get(i).equals("-v")) {
			verbose = true;
		}else if (clientRequest.get(i).equals("-h")) {

			headerList.add(clientRequest.get(i + 1));

		} else if (clientRequest.get(i).equals("-d")) {    // deal with --d ????

			inline = clientRequest.get(i + 1);

		} else if (clientRequest.get(i).equals("-f")) {

			path = clientRequest.get(i + 1);
		}
	}
		/**  // testing
		System.out.println(method);
		System.out.println(url);
		System.out.println(verbose);
		System.out.println(inline);		
		**/
		
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

		BufferedReader brr = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String statusCode = brr.readLine();

		// check for redirection ***********
		String str_temp;
		String[] array = statusCode.split(" ");
		if (array[1].contains("3")) {
			redirect = true;
			while ((str_temp = brr.readLine()) != null) {

				if (str_temp.startsWith("Location:"))  {
					newLocation = str_temp.split(" ")[1];
					break;
				}
			}
		}


		if(clientRequest.contains("-o")){
			String filePath=clientRequest.get(clientRequest.size()-1);

			FileWriter file=new FileWriter(filePath,true);
			BufferedWriter bufferWriter=new BufferedWriter(file);
			PrintWriter printWriter1=new PrintWriter(bufferWriter);

			// if request does contain 'verbose'(-v) command
			if(clientRequest.contains("-v")){
				printWriter1.println(statusCode);
				while((str_temp = brr.readLine()) !=null ) {
					printWriter1.println(str_temp);
					if(str_temp.equals("}"))
						break;
				}
			}
			// if request does not contain 'verbose'(-v) command
			else{
				int flag=0;
				while((str_temp = brr.readLine()) !=null ) {
					if(str_temp.trim().equals("{")) flag=1;
					if(flag==1){
						printWriter1.println(str_temp);
						if(str_temp.equals("}"))
							break;
					}
				}
			}
			printWriter1.flush();
			printWriter1.close();
		}
		// Printing response to the console
		else{
			if(clientRequest.contains("-v")){
				System.out.println(statusCode);
				while((str_temp = brr.readLine()) !=null ) {
					System.out.println(str_temp);
					if(str_temp.equals("}"))
						break;
				}
			}
			// if request does not contain 'verbose'(-v) command
			else{
				int flag=0;
				while((str_temp = brr.readLine()) !=null) {
					if(str_temp.trim().equals("{")) flag=1;
					if(flag==1){
						System.out.println(str_temp);
						if(str_temp.equals("}"))
							break;
					}
				}
			}
		}


		brr.close();
		
	
//************************************************************************
return verbose;
	
	
	}
}