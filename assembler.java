import java.util.Scanner;
import java.io.*;
import java.util.StringTokenizer;
import java.text.DecimalFormat;

public class assembler
{
	DecimalFormat df = new DecimalFormat("0000");
	
	String[][] OPTAB = new String[59][3];
	String[][] sourceCode;
	String[][] SYMTAB = {{"symbol","location"}};
	String[][] litPool = {{"literal","value"}};
   int LocStart;
	int programLength;
	int litPoolPtr = 0;
	int base;
	
	public assembler (){		}                                                         //預設建構子
	
	public assembler (String file)			//read source file                  //參數為檔案名稱,做組譯的動作
	{
      OPTAB = loadOPTAB("SIC_XE_Instruction_Set.txt");
      sourceCode = loadFromFile(file);
      if(file.contains("Figure") ){
         for(int j=0;j<sourceCode.length;j++)      //把資料讀進去
         {
            String temp=sourceCode[j][3];
            sourceCode[j][3]=sourceCode[j][2];
            sourceCode[j][2]=sourceCode[j][1];
            sourceCode[j][1]=sourceCode[j][0];
            sourceCode[j][0]=" ";
            
         }
      }
      sourceCode = modifyForm(sourceCode);       //修改sourceCode為組譯時適當格式
      
      pass1();
      pass2();
      save("ObjectCode.txt");
   }
	
	public String[][] loadOPTAB (String file)                             //從檔案中讀入OPTAB
	{
      Scanner inputStream = null;                                                          //初始化scanner
      String[][] optable = new String[OPTAB.length][OPTAB[0].length];       //宣告2維陣列來放OPTAB
     
      try
      {
         inputStream =
               new Scanner(new FileInputStream(file));
      }
      catch(FileNotFoundException e)
      {
         System.out.println("File "+file+" was not found");
         System.out.println("or could not be opened.");
         System.exit(0);
      }
      
      for(int i = 0;i<optable.length;i++)              //讀入OPTAB資料以便查詢
      {
         for(int j = 0;j<optable[i].length;j++)
         {
            optable[i][j] = inputStream.next();
         }
         
      }
      
      return optable;
	}
	
	public String[][] loadFromFile(String file)                                      //從檔案中讀入sourceCode
	{
	   Scanner inputStream = null;           //初始化scanner
		
		
		
	   String[][] source = new String[countRow(file)][6];
		
      	
      	try
   	   {
        	inputStream =  new Scanner(new FileInputStream(file));     //從檔案中讀入sourceCode     
         }
      	catch(FileNotFoundException e)
      	{
           	System.out.println("File "+file+" was not found");
           	System.out.println("or could not be opened.");
           	System.exit(0);
       	}
       	
       	int i = 0;
       	
       	while(inputStream.hasNextLine() )     //當還有下一行時
       	{
       	   String nextLine = inputStream.nextLine();
            StringTokenizer line = new StringTokenizer(nextLine);        //以空白為基準切開各字串
            
            //source = new String[i+1][];
            //source[i] = new String[6];
       	   
       	   int j;
       	   for(j = 0;line.hasMoreTokens() && j<6;j++ )        //讀入一列放到source裡面
            {
               switch(j)
               {
                  case 0:
                  case 1:
                  case 2:
                  case 3:
                     source[i][j] = line.nextToken();
                     break;
                  case 4:
                  case 5:
                     source[i][j] = " ";
                     break;
                  default :
                    System.out.println("ERROR");
               }

             
            }
            
            i++;         
       	}

       	inputStream.close();
       	
         return source;
	}
	
	public void save(String file)                                                       //將sourceCode存至檔案
	{
      PrintWriter outputStream = null;                  
      try
      {
          outputStream = 
               new PrintWriter(new FileOutputStream(file,false));             //開新檔案
      }
      catch(FileNotFoundException e)                      //無法打開時
      {
          System.out.println("Error opening the file "+file);
          System.exit(0);
      }
     
      for(int m = 0;m<sourceCode.length;m++ )         //把它印出來,m=列,n=行
      {
         for(int n =0;n<sourceCode[m].length;n++ )//
         {
            switch(n)
            {
               case 0:
               case 1:
               case 2:
               case 3:
               case 5:
                  outputStream.print(sourceCode[m][n]+"\t");            //斜線t對齊用
                  System.out.print(sourceCode[m][n]+"\t");
                  break;
               case 4:
                  outputStream.print(toBeautyFormat(sourceCode[m][n] ,16) );     //使排版美觀
                  System.out.print(toBeautyFormat(sourceCode[m][n] ,16));
                  break;
            }
            
         }
         outputStream.println();       //換行用
         System.out.println();
      }
      
      outputStream.close();               //關閉檔案
	}
	
	
	
   public String toBeautyFormat(String s ,int bit)                              //傳入字串和位數,不滿的位數補" "(空白)
   {
      String temp = s;
     
      for(int i=0;i<bit-s.length();i++ )            //補滿空白建
      {
         temp += " ";
      }
      
      temp = temp.toUpperCase();    //大寫
         
      return temp;
   }
	
   public int countRow(String file)                                                          //輸入檔案,回傳所需的rows數目
   {
         Scanner inputStream = null;
         int g = 0;                   //設原來行數為0
         
         try
          {
         inputStream =
            new Scanner(new FileInputStream(file));
        }
         catch(FileNotFoundException e)
         {
            System.out.println("File "+file+" was not found");
            System.out.println("or could not be opened.");
            System.exit(0);
         }
         
         
         while(inputStream.hasNextLine() )
         {
            inputStream.nextLine();
            g++;
         }
         
         return g;
   }
	
	public String[][] modifyForm(String[][] source)                     //修改sourceCode為組譯時適合的格式
	{
		for(int i = 0;i<source.length;i++)
		{
         if(isOpcode(source[i][1]) ||source[i][1].equalsIgnoreCase("base") ||source[i][1].equalsIgnoreCase("ltorg") )
         {
            source[i][3] = source[i][2];
            source[i][2] = source[i][1];
            source[i][1] = " ";           //null;
            if(source[i][2].equalsIgnoreCase("rsub") || source[i][2].equalsIgnoreCase("ltorg") ){
               source[i][3] = " ";        //null;
            }
            
         }
         
         if(source[i][1].contains(".") ){
            source[i][2] = " ";           //null;
            source[i][3] = " ";           //null;
            source[i][4] = " ";           //null;
         }
         source[i][4] = " ";           //null;
         source[i][5] = " ";           //null;
		}
	

	
		return source;
	}
	
	public boolean isOpcode(String token)                                                     //判斷token是否為Opcode
	{
	   String tempOp = token;
      StringTokenizer test = new StringTokenizer(token,"");
	   
      
	   
	   if(token.contains("+") ){    
	     tempOp = token.substring(1);
	     
	   }
	   if(isOpcode2(tempOp) != -1 ){
	      return true;
	   }
	   else{
	     return false;
	   }
	}
	
	public int isOpcode2(String token)                                                        //回傳token的format,不是則回傳-1
	{
	    int q = -1;
	   
	   for(int i = 0;i<OPTAB.length;i++)     //比對token是否存在OPTAB
	   {
	      if(token.equalsIgnoreCase(OPTAB[i][0]) ){
	         q = Integer.valueOf(OPTAB[i][2]);
	         break;
	      }
	   }

      if(token.equalsIgnoreCase("end") ){
         q = 0;
      }
      
      
      
	  
	   return q;
	}
	
	
//	
	public void pass1 ()                                                                      //PASS1
	{
      //System.out.println("--------------------PASS1----------------------");
  	   
  	   String[] tempLine = new String[6];
	   int location = 0x0;            //16進位
	   int index = 0;
	   String hax ;
	   int temp;            //location 的暫存值
	   String baseStr = "0000";
	    
	   tempLine = sourceCode[index];               
	   
	   if(tempLine[2].equalsIgnoreCase("start") ){
	      location = Integer.valueOf(tempLine[3],16 );
	      
         
	   }
	   else {
	     location = 0;
	   }
      sourceCode[0][4] = to4Xbits(Integer.toString(location,16),4);     //計算起始位置
      tempLine = sourceCode[++index];
	   
	   LocStart = location;
	   temp = location;
	   
	   while(!tempLine[2].equalsIgnoreCase("end") )
	   {
         
         if(!tempLine[1].contains(".")  ){
            sourceCode[index][4] = to4Xbits(Integer.toHexString(location),4);
	         
	                                                                          
	         
	         if(tempLine[1]!=" "){
               if(hasSymbol(tempLine[1]) ){         //判斷sym是否已存在SYMTAB中
                  tempLine[1] += "<."; 
               }
               else{
                  insertToSYMTAB(tempLine[1],location);         //插入sym,loc到SYMTAB中
               }
	         }
	         
	         location = temp;
	         
	         if(isOpcode(tempLine[2]) ){                     //是否為Opcode
	            if(isOpcode2(tempLine[2]) == 2 ){        
	              location += 2;
	              temp = location;
	            }
	            else{
	              if(tempLine[2].contains("+") ){
	                 location +=4;
	                 temp = location;
	              }
	              else {
	                 location +=3;
	                 temp = location;
	              }
	            }
	         }
	         else if(tempLine[2].equalsIgnoreCase("word") ){
	           location += 3;
	           temp = location;
              
	         }
            else if(tempLine[2].equalsIgnoreCase("resw")){
	           location += 3*Integer.valueOf(tempLine[3]);
	           temp = location;
              
	         }
            else if(tempLine[2].equalsIgnoreCase("resb")){
              location += Integer.valueOf(tempLine[3]);
              temp = location;
             
	         }
            else if(tempLine[2].equalsIgnoreCase("byte")){
              location += countByte(tempLine[3]);
              temp = location;
              
	         }
	         else if(tempLine[2].equalsIgnoreCase("base") ){
	           sourceCode[index][4] = " ";
	           baseStr = tempLine[3];
	         }
	         else if(tempLine[2].equalsIgnoreCase("ltorg")){
	           sourceCode[index][4] = " ";
	            addLitLines(index+1);
	         }
            else if(tempLine[2].equalsIgnoreCase("equ")){
              if(!tempLine[3].equals("*"))
              {
                for(int i=0;i<SYMTAB.length;i++)
                {
                  if(tempLine[1].equalsIgnoreCase(SYMTAB[i][0]) )
                     SYMTAB[i][1] = to4Xbits(Integer.toHexString(calculateExp(tempLine[3])),4);
                }
                sourceCode[index][4] = to4Xbits(Integer.toHexString(calculateExp(tempLine[3])),4);
              }
              else 
                     temp = location;
                 
              
            }
	         else if(tempLine[2].contains("=") ){
	           
	           location += countByte(tempLine[2]);
	           temp = location;
	          
	         }
	         else{
	           tempLine[2] += "<";
	         }
	         
	         if(tempLine[3].contains("=")){
	           if(newLiteral(tempLine[3]) )       //判斷是否為新的literal
	               insertToLitPool(tempLine[3]);     //插入sym到LitPool中
	         }
            
	      }
         tempLine = sourceCode[++index];
         
	     
	   }
	   
	   
	  
      int num = litPool.length-litPoolPtr-1;
	   if(num>0){                                           //若LitPool還有literal,則把其放至程式最下方,並給予location
	                                                               //最下面
	   
	     String[][] tempx = sourceCode;
	     sourceCode = new String[sourceCode.length+num][6];
	     
	     for(int i=0;i<sourceCode.length;i++)
	     {
	        if(i<sourceCode.length-num){
   	        
   	        for(int j=0;j<6;j++)
   	        {
   	           sourceCode[i][j] = tempx[i][j];
   	        }
	        }
	        else{
             
                 sourceCode[i][0] = " ";
                 sourceCode[i][3] = " ";
                 sourceCode[i][1] = "*";
                 sourceCode[i][2] = litPool[litPoolPtr+1][0];
                 sourceCode[i][4] = Integer.toString(location,16);
                 sourceCode[i][5] = " ";
                 location += countByte(litPool[litPoolPtr+1][0]);
                 litPoolPtr++;
                 
	        }
	     }
	   }
	   
	   base = valueOf(baseStr);
	   
	   programLength = location - LocStart;
	   
	   for(int i=0;i<sourceCode.length;i++)            //重新排版(4->1)
	   {
	     String tempy = sourceCode[i][4];
        sourceCode[i][4] = sourceCode[i][3];
        sourceCode[i][3] = sourceCode[i][2];
        sourceCode[i][2] = sourceCode[i][1];
        sourceCode[i][1] = tempy;
	   }
	   
	  
	   
	   
      
      
      
      
      
	}
	
	public int addLitLines(int index)                                            //處理LTORG:插入literal Pool中literal
	{
	  String[][] temp = sourceCode;
     int numOfLit = litPool.length-litPoolPtr-1;
	  sourceCode = new String[sourceCode.length+numOfLit][6];
	  
	  for(int i = 0;i<sourceCode.length;i++)
	  {
	     if(i<index){
	        for(int j = 0;j<6;j++)
   	     {
   	        sourceCode[i][j] = temp[i][j];
   	     }
	     }
	     
	     else if(i>=index+numOfLit){
	        for(int j = 0;j<6;j++)
	        {
	           sourceCode[i][j] = temp[i-numOfLit][j];
	        }
	     }
	  }
	  
	  
     
	  
	  for(int i = index;i<index+numOfLit;i++)
	  {
	     sourceCode[i][0] = " ";
        sourceCode[i][3] = " ";
        sourceCode[i][1] = "*";
        sourceCode[i][2] = litPool[i-index+litPoolPtr+1][0];
        sourceCode[i][4] = " ";
        sourceCode[i][5] = " ";
        litPoolPtr++;
	  }
	  
	  
	  
	  return numOfLit;
	}
	
	public int countByte(String token)                                                        //回傳literal佔幾個BYTE
	{
	  int b = 0;
	  StringTokenizer temp = new StringTokenizer(token,"'");
	  String type = temp.nextToken();
     String last = temp.nextToken();
	  
     if(type.equalsIgnoreCase("x") ){
        b = last.length()/2;
	  }
     else if(type.equalsIgnoreCase("c") ){
        b = last.length();
	  }
	  else if(token.contains("=") ){
	     token = token.substring(1,token.length()-1 );
	     b = countByte(token);
	  }
	  return b;
	} 
	
	public boolean hasSymbol(String sym)                                        //判斷sym是否已存在SYMTAB中
	{
	  boolean flag = false;
	  
	  for(int i = 0;i<SYMTAB.length;i++)
	  {
	     if(!SYMTAB[i][0].contains("*") ){
	     
   	     if(SYMTAB[i][0].equalsIgnoreCase(sym) ){
   	        flag = true;
   	        
   	        break;
   	     }
	     }
	  }
	  
	  return flag;
	}
	
	public void insertToSYMTAB(String sym,int loc)                      //插入sym,loc到SYMTAB中
	{
	   String[][] temp = SYMTAB;
	   SYMTAB = new String[SYMTAB.length+1][2];
	   
	   int i ;
	   
	   for(i = 0;i<SYMTAB.length-1;i++)
	   {
	     for(int j = 0;j<SYMTAB[i].length;j++)
	     {
	        SYMTAB[i][j] = temp[i][j];
	     }
	   }
	   
	   SYMTAB[i][0] = sym;
	   SYMTAB[i][1] = to4Xbits((Integer.toHexString(loc)),4);
	}
	
   public boolean newLiteral(String sym)                                                     //判斷是否為新的literal
   {
     boolean flag = true;
     
     for(int i = 0;i<litPool.length;i++)
     {
        if(litPool[i][0].equalsIgnoreCase(sym) ){
           flag = false;
           //System.out.println(SYMTAB[i][0]+","+sym);
           break;
        }
     }
     
     return flag;
   }
	
   public void insertToLitPool(String sym)                                                   //插入sym到LitPool中
   {
      String[][] temp = litPool;
      litPool = new String[litPool.length+1][2];
      
      int i ;
      
      for(i = 0;i<litPool.length-1;i++)
      {
        for(int j = 0;j<litPool[i].length;j++)
        {
           litPool[i][j] = temp[i][j];
        }
      }
      
      litPool[i][0] = sym;
      litPool[i][1] = litValue(sym);
   }
   
   public String litValue(String sym)                                                        //回傳literal的value
   {
      if(sym.contains("=") )
         sym = sym.substring(1,sym.length()-1 );
      
      StringTokenizer temp = new StringTokenizer(sym,"'");
      String value = "0";
      
      
      String type = temp.nextToken();
      String last = temp.nextToken();
      
      if(type.equalsIgnoreCase("X") ){
         value = last;
      }
      else if(type.equalsIgnoreCase("C") ){
         byte v[] = last.getBytes();
         value = Integer.toString(v[0],16);
         for(int i=1;i<v.length;i++ )
         {
            value += Integer.toString(v[i],16);
         }
         
      }
      value = value.toUpperCase();
      
      return value;
   }
   
   public int calculateExp(String exp)                                                       //計算EQU的運算式
   {
      byte[] g = exp.getBytes();
      String a,b,c=null,x,y=null;
      int index=0;
      int result=0;
      
      //System.out.println(index);
      char m = (char)g[index];
      char n = (char)g[index];
      
      
      a = Character.toString((char)g[index++] );
      
     
      while(!findInSYMTAB(a) ){                       //
         a += Character.toString((char)g[index++] );
      }
      
      x = Character.toString((char)g[index++] );
      
      b = Character.toString((char)g[index++] );
      while(!findInSYMTAB(b) ){
         b += Character.toString((char)g[index++] );
      }
      
      if(index < g.length ){
         y = Character.toString((char)g[index++] );
         
         c = Character.toString((char)g[index++] );
         while(!findInSYMTAB(c) ){
            c += Character.toString((char)g[index++] );
         }
      }
      
      if(x.equals("+") ){
         result = valueOf(a) + valueOf(b);            //
      }
      else if(x.equals("-") ){
         result = valueOf(a) - valueOf(b);
      }
      
      
      if(c != null){
         if(y.equals("+") ){
            result += valueOf(c);
         }
         else if(y.equals("-") ){
            result -= valueOf(c);
         }
      }
      
      return result;
   }
   
   public boolean findInSYMTAB(String s)                                                     //看s有沒有存在SYMTAB中
   {
      boolean flag = false;
      
      for(int i=0;i<SYMTAB.length;i++)
      {
         if(s.equalsIgnoreCase(SYMTAB[i][0]) ){
            flag = true;
            break;
         }
      }
      
      return flag;
   }
   
   public int valueOf(String t)                                                              //回傳symbol的location
   {
      int k = -1;
      
      for(int i=0;i<SYMTAB.length;i++)
      {
         if(t.equalsIgnoreCase(SYMTAB[i][0]) ){
            k = Integer.valueOf(SYMTAB[i][1],16);
         }
         
      }
      
      return k;
   }
   
   public String to4Xbits(String s ,int bit)                                                 //傳入字串和位數,不滿的位數補0
   {
      String temp = s;
      /*if(s.length() == bit ){
         temp = s;
      }
      else{*/
      for(int i=0;i<bit-s.length();i++ )
      {
         temp = "0"+temp;
      }
      
      temp = temp.toUpperCase();  
         
      return temp;
   }
   
   public void pass2()                                                                       //PASS2
   {
      String[] tempLine = new String[6];
      int index = 0;
      String objectCode = "000000";
      
      tempLine = sourceCode[index++];
      if(tempLine[3].equalsIgnoreCase("start") ){
         tempLine = sourceCode[index++];
      }
      while(!tempLine[3].equalsIgnoreCase("end") )
      {
         if(!tempLine[2].contains(".") && !tempLine[3].equalsIgnoreCase("base") && !tempLine[3].equalsIgnoreCase("ltorg")
                       && !tempLine[3].equalsIgnoreCase("resw") && !tempLine[3].equalsIgnoreCase("resb") && !tempLine[3].equalsIgnoreCase("equ")){
            if(getFormat(tempLine[3]) == 2 ){                                                      //format 2
               String opcode = Integer.toString(getOpcode(tempLine[3]),16);
               String a=null,b=null;
               
               StringTokenizer e = new StringTokenizer(tempLine[4],",");
               a = e.nextToken();
               if(e.hasMoreTokens() ){
                  b = e.nextToken();
               }
               a = numOfRegister(a);
               b = numOfRegister(b);
               
               objectCode = opcode+a+b;
               objectCode = objectCode.toUpperCase();
               //System.out.println(objectCode);
            }
            else if(getFormat(tempLine[3]) == 3 && !tempLine[3].contains("+") ){                   //format 3
                int q = 1;
                String nextAddr = sourceCode[index][1];
                while(nextAddr.equalsIgnoreCase(" ") ){
                  nextAddr = sourceCode[index+q][1];
                  q++;
                }
                objectCode = format3(tempLine[3] ,tempLine[4] ,nextAddr);
                //System.out.println(objectCode);
            }
            else if(getFormat(tempLine[3]) == 3 && tempLine[3].contains("+") ){                    //format 4
               objectCode = format4(tempLine[3],tempLine[4]);
               //System.out.println(objectCode);
            }
            else if(tempLine[3].contains("=") ){                                                   //literal
               objectCode = getLitValue(tempLine[3]);
               //System.out.println(objectCode);
            }
            else if(tempLine[3].equalsIgnoreCase("byte") ){                                        //byte
               objectCode = litValue(tempLine[4]);
               objectCode = objectCode.toUpperCase();
               //System.out.println(objectCode);
            }
            else{
               System.out.println((index-1)+":ERROR-->"+tempLine[3] );
            }
            
            sourceCode[index-1][5] = objectCode;
         }
         tempLine = sourceCode[index++];
      }
      
      for(int i=index;i<sourceCode.length;i++)       //填入literal Value
      {
         if(sourceCode[i][3].contains("=") ){
            sourceCode[i][5] = getLitValue(sourceCode[i][3]);
            
         }
      }
      
      //printOut(sourceCode);
   }
   
   public String format3(String mnem ,String para ,String srcAddrInHex)                      //處理PASS2中format3的情形
   {
      int op = getOpcode(mnem);
      String opcode;
      int srcAddr = Integer.valueOf(srcAddrInHex ,16);
      int destAddr;
      String objectCode = "ERROR";
      int type = 0;
      String dispStr = "000";
      int disp = 0;
      
      if(para.contains("#") ){
         para = para.substring(1 ,para.length() );
         if(findInSYMTAB(para) ){                                                         //#MIXLEN 這類
            opcode = to4Xbits(Integer.toString(op+1,16),2);
            destAddr = valueOf(para);
            disp = destAddr-srcAddr;
            if(disp >= -2048 && disp <=2047){                     //pc
               type = 2;
               if(disp >=0){
                  dispStr = to4Xbits(Integer.toString(disp,16) ,3);
               }
               else{
                  dispStr = to4Xbits(Integer.toString(disp+4096,16) ,3);
               }
            }
            else{                                                                         //base relative
               type = 4;
               dispStr = to4Xbits(Integer.toString(destAddr - base ,16) ,3);
            }
            objectCode = opcode+Integer.toString(type ,16)+dispStr;
            
            para = "#" + para;
         }
         else{                                                                            //#3 這類
            opcode = to4Xbits(Integer.toString(op+1,16),2);
            type = 0;
            dispStr = to4Xbits(Integer.toString(Integer.valueOf(para) ,16) ,3);
            
            objectCode = opcode+Integer.toString(type ,16)+dispStr;
         }
      }
      else{
         if(para.equalsIgnoreCase(" ") ){                                                 //RSUB
            opcode = to4Xbits(Integer.toString(op+3,16),2);
            objectCode = opcode+"0000";
         }
         else {
            if(para.contains("@") ){                                                         //有"@"
               para = para.substring(1 ,para.length() );
               opcode = to4Xbits(Integer.toString(op+2,16),2);
            }
            else{                                                                            //沒有"@"
               opcode = to4Xbits(Integer.toString(op+3,16),2);
            }
            
            if(para.contains(",X") ){                                                        //是index mode
               para = para.substring(0 ,para.length()-2 );
               type += 8;
            }
            
            destAddr = valueOf(para);
            if(para.contains("=") ){                                                         //para is a literal
               destAddr = getLocInSourceCode(para);
            }
            
            disp = destAddr-srcAddr;
            if(disp >= -2048 && disp <=2047){                                             //pc
               type = 2;
               if(disp >=0){
                  dispStr = to4Xbits(Integer.toString(disp,16) ,3);
               }
               else{
                  dispStr = to4Xbits(Integer.toString(disp+4096,16) ,3);
               }
            }
            else{                                                                         //base relative
               type += 4;
               dispStr = to4Xbits(Integer.toString(destAddr - base ,16) ,3);
            }
            
            /*if(!hasSymbol(para) ){
               objectCode = "******" + objectCode;
               System.out.println("----------------------------");
            }*/
            
            objectCode = opcode+Integer.toString(type ,16)+dispStr;
            
         }
      }
      
      /*if(!para.contains("#") && !para.contains("@") && !para.equalsIgnoreCase(" ") && !para.contains(",X") && !para.contains("=") && !hasSymbol(para)   ){
               objectCode = "^" + objectCode;
               System.out.println(para);
            }
      */
      objectCode = objectCode.toUpperCase();
      return objectCode;
   }
      
   public String format4(String mnem ,String para )                                          //處理PASS2中format4的情形
   {
      int op = getOpcode(mnem);
      String opcode;
      //int srcAddr = Integer.valueOf(srcAddrInHex ,16);
      String objectCode = "ERROR";
      
      
      if(para.contains("#") ){
         para = para.substring(1,para.length());
         opcode = to4Xbits(Integer.toString(op+1,16),2)+"1";
         if(addrInSYMTAB(para) == -1 ){
            objectCode = opcode + to4Xbits(Integer.toString(Integer.valueOf(para),16) ,5);
         }
         else{
            objectCode = opcode + to4Xbits(Integer.toString(addrInSYMTAB(para),16),5 );
         }
         
      }
      else{
         opcode = to4Xbits(Integer.toString(op+3,16),2)+"1";
         objectCode = opcode + to4Xbits(Integer.toString(addrInSYMTAB(para),16),5 );
            
      }
      
      return objectCode;
   }
   
   public int getLocInSourceCode(String para)
   {
      int loc = 0;
      for(int i=0;i<sourceCode.length;i++)
      {
         if(para.equalsIgnoreCase(sourceCode[i][3] )){
            loc = Integer.valueOf(sourceCode[i][1] ,16);
            break;
         }
      }
      return loc;
   }
   
   public int addrInSYMTAB(String para)                                                     //回傳para的location
   {
      int addr = -1;
      
      for(int i=0;i<SYMTAB.length;i++)
      {
         if(SYMTAB[i][0].equalsIgnoreCase(para) ){
            addr = Integer.valueOf(SYMTAB[i][1],16);
         }
      }
      
      return addr;
   }
   
   public String numOfRegister (String m)                                                    //回傳register的代號
   {
      String n;
      
      if("a".equalsIgnoreCase(m) ){
         n = "0";
      }
      else if("x".equalsIgnoreCase(m) ){
         n = "1";
      }
      else if("s".equalsIgnoreCase(m) ){
         n = "4";
      }
      else if("t".equalsIgnoreCase(m) ){
         n = "5";
      }
      else{
         n = "0";
      }
      
      return n;
   }
   
   public int getFormat(String mnem)                                                         //get symbol的format
   {
      int format = -1;
      
      if(mnem.contains("+") ){
         mnem = mnem.substring(1,mnem.length() );
      }
      
      //System.out.println(mnem);
      
      for(int i=0;i<OPTAB.length;i++)
      {
         if(mnem.equalsIgnoreCase(OPTAB[i][0]) ){
            format = Integer.valueOf(OPTAB[i][2],10);
         }
      }
      return format;
   }
   
   public int getOpcode(String mnem)                                                         //get symbol的Opcode
   {
      int opcode = -1;
      
      if(mnem.contains("+") ){
         mnem = mnem.substring(1,mnem.length() );
      }
      
      for(int i=0;i<OPTAB.length;i++)
      {
         if(mnem.equalsIgnoreCase(OPTAB[i][0]) ){
            opcode = Integer.valueOf(OPTAB[i][1],16);
         }
      }
      return opcode;
   }
   
   public String getLitValue(String lit)                                                     //回傳literal的value
   {
      String val = "00";
      
      for(int i=0;i<litPool.length;i++)
      {
         if(lit.equalsIgnoreCase(litPool[i][0]) ){
            val = litPool[i][1];
         }
      }
      
      return val;
   }
	
	public static void main (String[] args)
	{
		Scanner input = new Scanner(System.in);
		
		for(int i=0;i<50;i++){
		 System.out.println();
		}
		
		System.out.print("請輸入SourceCode檔名:");
		String x = input.next();
		
		assembler b = new assembler(x);
		
		//b.pass1();
		//b.pass2();
		//b.printOut(b.sourceCode);
		//b.save("ObjectCode.txt");
		//b.printOut(b.litPool);
		
		
	}
}