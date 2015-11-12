//Ong Mu Sen Jeremy A0108310Y
import java.security.*;
import java.util.*;
import java.io.*;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class BeautifulRainbowTable {

	private static HashMap<String, byte[]> table;
	public static byte[][] inputs;
    private static MessageDigest SHA;
    private static final int CHAIN = 190;//305, 285, 275(BEST without zip), 265;
    private static final int ROWS = 50000; //30000, 32000, 34000(BEST without zip), 36000;

	public static void main(String[] args) throws Exception {
		//Build
		long start;
		long end;
		byte[] plain;
		byte[] word;
		String key;
		table = new HashMap<String, byte[]>();
		SHA = MessageDigest.getInstance("SHA1");
		inputs = new byte[ROWS][20];
		int success = 0;
		int collide = 0;
		int i = 0;
		start = System.currentTimeMillis();
		while(table.size() < ROWS) {
			plain = intToBytes(i);
			word = singleChain(plain, i);
			key = bytesToHex(word);
			if(!table.containsKey(key)) {
				table.put(key, plain);
				success++;
			} else {
				collide++;
			}
			i++;
		}
		end = System.currentTimeMillis();
		System.out.println("Table: " + (end-start)/1000.0 + " seconds.");
		writeToFile();
		
		//Speed
		byte[] theWord = new byte[3];
		Random r = new Random(30);
		r.nextBytes(theWord);
		start = System.currentTimeMillis();
		for(int j = 0; j < 8388608; j++) {
			byte[] useless = hashing(theWord);
		}
		end = System.currentTimeMillis();
		System.out.println("T = " + (end-start)/1000.0);
		
		//Matching
		String fileName;
		Scanner sc = new Scanner(System.in);
		fileName = sc.nextLine();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String curLine;
		int hit = 0;
		int num = 0;
		byte[][] digests = new byte[1000][20];
		byte[][] words = new byte[1000][3]; 
		while((curLine = br.readLine()) != null) {
			String hexString;
			hexString = curLine.substring(2,10) + curLine.substring(12,20) + curLine.substring(22,30) + curLine.substring(32,40) + curLine.substring(42,50);
			hexString = hexString.replaceAll("\\s", "0");
            //System.out.println(hexString);
			digests[num] = hexToBytes(hexString);
            num++;
		}
		br.close();
		FileWriter fw = new FileWriter("word.data");
		byte[] currentDigest;
		byte[] answer;
		long starting = System.currentTimeMillis();
		for(int a = 0; a < words.length; a++) {
			currentDigest = digests[a];
			answer = invert(currentDigest);
			words[a] = answer;
			if(answer != null) {
				hit++;
			}
		}
		long ending = System.currentTimeMillis();
		for(int b = 0; b < words.length; b++) {
			if(words[b] == null) {
				fw.write("\n 0");
			} else {
				fw.write("\n " + bytesToHex(words[b]));
			}
		}
		fw.write("\n\nWords found: " + hit + "\n");
		fw.close();
		System.out.println("INVERT takes: " + (ending - starting)/1000.0 + " seconds.");
        System.out.println("Words found: " + hit);
        sc.close();
	}

	private static byte[] singleChain(byte[] plain, int ti) throws Exception {
        byte[] hash = new byte[20];
        byte[] word = plain;
        for (int i = 0; i < CHAIN; i++) {
            hash = hashing(word);
            word = reduction(hash, i);
        }
        return word;
    }

    private static byte[] hashing(byte[] text) {
        byte hash[] = new byte[20];
        try {
            hash = SHA.digest(text);
            SHA.reset();
        } catch (Exception e) {
        }
        return hash;
    }
    
    private static byte[] reduction(byte[] digest, int len) {
        byte last_byte = (byte) len;
        byte[] word = new byte[3];
        for (int i = 0; i < word.length; i++) {
            word[i] = (byte) (digest[(len + i) % 20] + last_byte);
        }
        return word;
    }

    private static byte[] invert(byte[] matching) {
        byte[] result = new byte[3];
        String key = "";
        for (int i = CHAIN - 1; i >= 0; i--) {
            key = invertHR(matching, i);
            if (table.containsKey(key)) {
                result = invertChain(matching, table.get(key));
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    private static byte[] invertChain(byte[] matching, byte[] word) {
        byte[] hash;
        for (int i = 0; i < CHAIN; i++) {
            hash = hashing(word);
            if (Arrays.equals(hash, matching)) {
                return word;
            }
            word = reduction(hash, i);
        }
        return null;
    }
    
    private static String invertHR(byte[] digest, int start) {
        byte[] word = new byte[3];
        for (int i = start; i < CHAIN; i++) {
            word = reduction(digest, i);
            digest = hashing(word);
        }
        return bytesToHex(word);
    }

    private static String bytesToHex(byte[] bytes) {
        HexBinaryAdapter change = new HexBinaryAdapter();
        String str = change.marshal(bytes);
        return str;
    }

    private static byte[] hexToBytes(String hexString) {
        HexBinaryAdapter change = new HexBinaryAdapter();
        byte[] bytes = change.unmarshal(hexString);
        return bytes;
    }
    private static byte[] intToBytes(int cur) {
        byte text[] = new byte[3];
        text[0] = (byte) ((cur >> 16) & 0xFF);
        text[1] = (byte) ((cur >> 8) & 0xFF);
        text[2] = (byte) cur;
        return text;
    }

    private static void writeToFile() {
        ObjectOutputStream OPS;
        try {
            OPS = new ObjectOutputStream(new FileOutputStream("table.data"));
            OPS.writeObject(table);
            OPS.close();
        } catch (Exception e) {
        }
    }
}