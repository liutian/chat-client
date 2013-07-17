package com.visionet.chat;

import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;


/* =======================��½���======================== */
public class SimpleChat extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField jtf=new JTextField();
	JButton jb=new JButton("ȷ��");
	Label l=new Label("�ǳƣ�");
	Label l2=new Label();
	String name="";
	DatagramSocket dgs=null;
	DatagramSocket dgs2=null;
	/*
	 * �������
	 */
	public static void main(String[] ag) {
		SimpleChat lp=new SimpleChat();
		lp.launch();
	}
	
	public void launch(){
		/*
		 * ���Ƶ�¼����
		 */
		this.addWindowListener(new LogClose());
		this.setTitle("��ƽ�������¼����");
		LogMouseAdapter lma=new LogMouseAdapter(this);
		this.setBounds(200, 100, 300, 150);
		this.setLayout(null);
		l.setBounds(75,23,40,30);
		l2.setBounds(220, 25, 80, 30);
		l.setFont(new Font("����",Font.PLAIN,17));
		jtf.setBounds(120,25,100,25);
		jb.setBounds(110,70 ,80,25);
		jb.addMouseListener(lma);
		this.add(l);
		this.add(jtf);
		this.add(jb);
		this.add(l2);
		this.setVisible(true);
	}
	/*
	 * ��¼��ť������
	 */
	class  LogMouseAdapter extends MouseAdapter{
		JFrame jf=new JFrame();
		String id="";
		String locationid="";
		byte []buf=new byte[1024];
		DatagramPacket dgp=null;
		/*
		 * ע�ᰴť�ļ�����
		 */
		LogMouseAdapter(JFrame jf){
			this.jf=jf;
			
			try{
				locationid=InetAddress.getLocalHost().toString();
				int start=InetAddress.getLocalHost().toString().indexOf("/");
				int end=InetAddress.getLocalHost().toString().lastIndexOf(".");
				id=InetAddress.getLocalHost().toString().substring(start+1, end+1);
			}catch(UnknownHostException uhe){
				System.out.println("��ȡ���ص�ַʧ��");
				uhe.printStackTrace();
			}
		}
		/*
		 * ��������
		 */
		public void mousePressed(MouseEvent ae) {
			name=jtf.getText().trim();		
			if(name.length()<2){
				l2.setText("�ǳƹ���");
			}else if(name.length()>7){
				l2.setText("�ǳƹ���");
			}else if(name.indexOf("#")!=-1||name.indexOf("Ⱥ��Ϣ")!=-1
			||name.indexOf("*")!=-1||name.indexOf("����Ա")!=-1){
				l2.setText("�ǳƷǷ�");
			}else {
				jf.setTitle("��½��...");
				jf.setEnabled(false);
				/*
				 * �����߳������շ����������ĵ�ַ
				 */
				CheckThread ct=new CheckThread(jf);
				FirstReceive ft=new FirstReceive();
				ft.launch(jf, name, ct,jtf);
				ct.launch(ft);
				ft.start();
				ct.start();
				/*
				 * ���������������
				 */
				try{
					buf=(name+"/"+InetAddress.getLocalHost().toString()).getBytes();
				}catch(UnknownHostException uhe){
					System.out.println("��ȡ���ص�ַʧ��");
					uhe.printStackTrace();
				}
				
				try {
					dgs=new DatagramSocket(2000);
				} catch (SocketException e) {
					e.printStackTrace();
				}
				
				for(int i=1;i<=255;i++){
					try{
						dgp=new DatagramPacket(buf,buf.length,new InetSocketAddress(id+i,3000));
						dgs.send(dgp);
					}catch(IOException ie){
						System.out.println("�������ݰ�ʧ��");
						System.out.println(id+i);
						ie.printStackTrace();
					}
				}
				dgs.close();
			}
		}
	}
	/*
	 *��ʱ��
	 */
	class CheckThread extends Thread{
		JFrame jf=null;
		FirstReceive t=new FirstReceive();
		
		CheckThread(JFrame jf){
			this.jf=jf;
		}
		
		private void launch(FirstReceive t){
			this.t=t;
		}
		
		public void run(){
			try {
				this.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("�̱߳����");
				e.printStackTrace();
				return;
			}
			t.interrupt();
			dgs2.close();
			jf.setEnabled(false);
			jf.setVisible(false);
			ServerPane sp=new ServerPane();
			sp.setVisible(true);
		}
	}
	/*
	 * ���շ�������Ϣ
	 */
	class FirstReceive extends Thread{
		byte [] buf=new byte[1024];
		DatagramPacket dgp=new DatagramPacket(buf,buf.length);
		String serverId="";//�����������ĵ�ַ
		JFrame jf=null;
		boolean bool=true;//�жϻ�ȡ�ķ�������ַ�Ƿ���ȷ
		String serid="";//���������ķ�������ַ
		Thread t=null;
		JTextField jtf=null;
		
		public void launch(JFrame jf,String name,CheckThread t,JTextField jtf){
			this.t=t;
			this.jf=jf;
			this.jtf=jtf;
		}
		
		public void run(){
			
			try{
				dgs2=new DatagramSocket(2020);
			}catch(IOException ie){
				System.out.println("�˿ڿ���ʧ��");
				ie.printStackTrace();
			}
			
			try{
				dgs2.receive(dgp);
				serverId=new String(buf,0,dgp.getLength());
			}catch(IOException ie){
				System.out.println("��������ַ����ʧ��");
				ie.printStackTrace();
				l2.setText("������������");
			}
			
			if(serverId.equalsIgnoreCase("#repeat:")){
				jf.setEnabled(true);////////////////////////////////////////////////////////////////////
				t.interrupt();
				l2.setText("�ǳ��ظ�");
			}else if(serverId.equals("")){
				l2.setText("������Ϣ����");
			}else{
				t.interrupt();///////////////////////////////////////////////////////////////////
				jf.setEnabled(false);
				jf.setVisible(false);
				Pane p=new Pane(serverId,name);
				p.setVisible(true);
			}
			dgs2.close();
		}
	}
	class LogClose extends WindowAdapter{
		
		public void windowClosing(WindowEvent we){
			if(dgs2!=null){dgs2.close();}
			if(dgs!=null){dgs.close();}
			System.exit(0);
		}
	}
}

/* ================�����================= */
class Pane extends JFrame implements ActionListener, Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String serverId="";//�洢��������ַ
	String name="";//�洢�ǳ�
	HashMap<String ,StringBuffer> lists=new HashMap<String,StringBuffer>();
	ArrayList<String> arrName=new ArrayList<String>(); 
	
	Socket s = null;
	DataOutputStream dos=null;
	DataInputStream dis=null;
	String now_chatObject="����Ա��Ϣ";
	Time now_Time=null;
	
	JTextPane text1 = new JTextPane();   //������ʾ��
	JTextPane text2 = new JTextPane();   //���������
	JScrollPane sc1 = new JScrollPane(text1, 22, 32);//������ʾ������
	JScrollPane sc2 = new JScrollPane(text2, 22, 32);//�������������
	
	JComboBox cb1 = null;//��������
	JComboBox cb2 = new JComboBox(new String[] { "����", "����", "б��", "��б��" });
	JComboBox cb3 = new JComboBox();//�����С
	
	DefaultListModel listmod = new DefaultListModel();
	JList jlist = new JList(listmod);//�����б��
	JScrollPane sc3 = new JScrollPane(jlist, 22, 32);//�����б��
	
	JButton btn1 = new JButton("����");
	JButton btn2 = new JButton("����");
	JButton btn3 = new JButton("����");
	JSplitPane sp0= new JSplitPane(0);//����
	JSplitPane sp1= new JSplitPane(1);//����
	JSplitPane sp2= new JSplitPane(0);//����
	
	Font font = new Font("����", 1, 12);
	Font[] font_array;
	String[] font_name;
	
	String clientId="";
	int transPort=20000;
	HashMap<String,TransPane> arrTransPane=new HashMap<String,TransPane>();

	/* =================����幹�췽��================= */

	Pane(String id, String name) {
		this.serverId=id;
		this.name = name;
		
		try {
			clientId=InetAddress.getLocalHost().toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		clientId=clientId.substring(clientId.lastIndexOf("/")+1);
		
		arrName.add("Ⱥ��Ϣ");
		arrName.add("����Ա��Ϣ");
		listmod.add(this.listmod.size(), "Ⱥ��Ϣ");
		listmod.add(this.listmod.size(),"����Ա��Ϣ");
		
		launch();
		/*
		 * �����߳�
		 */
		Thread client_thread = new Thread(this);
		client_thread.start();
		/*
		 * �������
		 */
		setBounds(100, 200, 500, 500);
		setTitle("��ӭ��:" + name);
		GraphicsEnvironment fm = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		font_name = fm.getAvailableFontFamilyNames();
		cb1 = new JComboBox(font_name);
		for (int i = 6; i <= 64; i++){
			cb3.addItem(i);
		}
		
		sc1.setHorizontalScrollBarPolicy(31);
		sc2.setHorizontalScrollBarPolicy(31);
		sc3.setHorizontalScrollBarPolicy(31);
		
		sp0.setTopComponent(sc1);
		sp0.setBottomComponent(sc2);
		sp0.setDividerLocation(280);
		sp1.setLeftComponent(sp0);
		sp1.setRightComponent(sc3);
		sp1.setDividerLocation(370);
		sp2.setTopComponent(sp1);
		
		getContentPane().setBackground(new Color(131, 172, 113));
		JPanel pan2 = new JPanel(new GridLayout(1, 6));
		pan2.add(btn1);//����
		pan2.add(btn2);//����
		pan2.add(btn3);//����
		pan2.add(cb1);
		pan2.add(cb2);
		pan2.add(cb3);
		
		btn1.addActionListener(this);
		btn2.addActionListener(this);
		btn3.addActionListener(this);
		cb1.addActionListener(this);
		cb2.addActionListener(this);
		cb3.addActionListener(this);
		sp2.setBottomComponent(pan2);
		sp2.setDividerLocation(442);
		
		add(sp2, "Center");
		setResizable(false);
		jlist.setSelectedIndex(0);
		sp0.setOneTouchExpandable(true);
		sp1.setOneTouchExpandable(true);
		jlist.addMouseListener(new JListMouseAdapter(this));
		lists.put("Ⱥ��Ϣ", new StringBuffer(""));
		lists.put("����Ա��Ϣ", new StringBuffer("��ӭ��ʹ�õ�ƽ��������������ߣ�������"));
		text1.setText("��ӭ��ʹ�õ�ƽ��������������ߣ�������");
		
		addWindowListener(new PaneWindowAdapter());
	}
	/*
	 * ���ӷ�����
	 */
	public void launch(){
		try{
System.out.println(serverId);
			s=new Socket(serverId,3333);
		}catch(IOException ie){
			System.out.println("�˿ڿ���ʧ��");
			ie.printStackTrace();
			System.exit(0);
		}
		
		try {
			dos=new DataOutputStream(s.getOutputStream());
			dis=new DataInputStream(s.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			dos.writeUTF(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 * ������Ϣ
	 */
	public void run() {
		String ins="";
		int no=-1;
		String str="";
		String Id="";
		String port="";
		
		while(true){
			try {
				ins=dis.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			
			if(ins.indexOf("#add:")!=-1){
				name=ins.substring(5);
				if(!lists.containsKey(name)){
					lists.put(name, new StringBuffer(""));
					arrName.add(name);
					listmod.add(listmod.size(), name);
				}
			}
			
			if(ins.indexOf("#leave:")!=-1){
				name=ins.substring(7);
				if(lists.containsKey(name)){
					lists.remove(name);
					no=arrName.indexOf(name);
					arrName.remove(name);
					listmod.remove(no);
				}
			}
			
			if(ins.indexOf("#name:")!=-1){
				no=ins.indexOf("#", 6);
				name=ins.substring(6, no);
				str=ins.substring(no+1);
				
				lists.get(name).append("\n\n"+name+"����˵��"+str);
				
				if(now_chatObject.equals(name)){
					text1.setText(lists.get(name).toString());
				}else{
					no=arrName.indexOf(name);
					if(listmod.get(no).toString().indexOf("*")==-1){
						listmod.set(no, name+"*");
					}
				}
			}
			
			if(ins.indexOf("#group:")!=-1){
				no=ins.indexOf("#",7);
				name=ins.substring(7, no);
				str=ins.substring(no+1);
				
				lists.get("Ⱥ��Ϣ").append("\n\n"+name+"˵��"+str);
				
				if(now_chatObject.equals("Ⱥ��Ϣ")){
					text1.setText(lists.get("Ⱥ��Ϣ").toString());
				}
			}
			
			if(ins.indexOf("#server:")!=-1){
				str=ins.substring(8);
				lists.get("����Ա��Ϣ").append("\n\n����Ա��"+str);
				
				now_chatObject="����Ա��Ϣ";
				this.setTitle("�鿴����Ա��Ϣ");
				text1.setText(lists.get("����Ա��Ϣ").toString());
			}
			
			if(ins.indexOf("#serverClosed:")!=-1){
				System.exit(0);
			}
			
			if(ins.indexOf("#trans:")!=-1){
				name=ins.substring(7,ins.lastIndexOf("#"));
				int len=Integer.parseInt(ins.substring(ins.lastIndexOf("#")+1));
				TransPane tp=new TransPane(" �� "+name);
				tp.transClientPane(name+"�����ļ���������","����","�ܾ�","�ͻ���",len);
			}
			
			if(ins.indexOf("#transId:")!=-1){
				name=ins.substring(9,ins.indexOf("#",9));
				Id=ins.substring(ins.indexOf("#",9)+1,ins.lastIndexOf("#"));
				port=ins.substring(ins.lastIndexOf("#")+1);
System.out.println("�������˽��յ��Ķ˿ں�Ϊ"+port);
				
				if(arrTransPane.containsKey(name)){
					arrTransPane.get(name).startupTrans(Id, port);
				}
			}
		}
	}
	/*
	 * ������Ϣ
	 */
	public void send(String ins) {
		try {
			dos.writeUTF(ins);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/*
	 *�����������¼������� 
	 */
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		if (o == btn1) {//���Ͱ�ť
			now_Time=new Time(System.currentTimeMillis());
			if(now_chatObject.equals("Ⱥ��Ϣ")){
				lists.get("Ⱥ��Ϣ").append("\n\n"+"��˵��"+text2.getText()+"  "+now_Time.toString());
				text1.setText(lists.get(now_chatObject).toString());
				send("#group:"+text2.getText()+"  "+now_Time.toString());
				text2.setText("");
			}
			else if(now_chatObject.equalsIgnoreCase("����Ա��Ϣ")){
				
			}
			else{
				lists.get(now_chatObject).append("\n\n"+"���"+now_chatObject+"˵��"+text2.getText()+"  "+now_Time.toString());
				text1.setText(lists.get(now_chatObject).toString());
				send("#name:"+now_chatObject+"#"+text2.getText()+"  "+now_Time.toString());
				text2.setText("");
			}
		} 
		else if (o == btn2) { //���䰴ť
			TransPane tp=new TransPane(" �� "+now_chatObject);
			File []fs=null;
			
			if(now_chatObject.equals("Ⱥ��Ϣ")){
				//////////////////////////////////////////////////////////////////
			}else{
				JFileChooser chooser=new JFileChooser();
				chooser.setMultiSelectionEnabled(true);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returVal=chooser.showOpenDialog(btn2);
				if(returVal==JFileChooser.APPROVE_OPTION){
					fs=chooser.getSelectedFiles();
					send("#trans:"+now_chatObject+"#"+fs.length);
				}
				tp.transServerPane("   �ȴ��Է��������ӣ����Ժ�......","ȷ��","��������",fs);
			}
			
			arrTransPane.put(now_chatObject,tp);
		} 
		else if(o == btn3){//���ذ�ť
			//////////////////////////////////////////////////////////////////////
		}
		else if (o == cb1 || o == cb2 || o == cb3) {//���尴ť
			String fontname = String.valueOf(cb1.getSelectedItem());
			int style = cb2.getSelectedIndex();
			int size = 6 + cb3.getSelectedIndex();
			font = new Font(fontname, style, size);
			text1.setFont(font);
			text2.setFont(font);
		}
	}
	/*
	 * �ļ�������
	 */
	public class Trans{
		byte []buf=new byte[2048];//��ʱ�洢���������ֽڵ�����
		int len=-1;//�Ѵ��ͳ�����յ����ֽ���
		
		ServerSocket ss=null;//����ͻ��˵�����
		Socket s=null;//����ͻ��˻�������˵�����
		/*
		 * ����ͻ�����ص���
		 */
		ObjectInputStream ois=null;
		DataInputStream dis=null;
		/*
		 * �������������ص���
		 */
		ObjectOutputStream oos=null;
		DataOutputStream dos=null;
		
		File []arrFile=null;
		File []fOutputs=null;//Ҫ���͵��ļ����ļ���
		File fOutput=new File("temp");
		File fObj=null;
		File fInput=null;
		int fileNum=0;//�Ѵ��ͻ���յ��ļ���
		
		TransPane tp=null;//�������
		
		String Id="";//���Ϳͻ��˵�ַ
		int port=-1;//���Ϳͻ��˿����Ķ˿�
		/*
		 * ����ͻ���
		 */
		Trans(TransPane tp,File fInput){
			this.tp=tp;
			this.fInput=fInput;
		}
		/*
		 * �����������
		 */
		Trans(TransPane tp,File[] fOutputs,String Id,String port){
			this.tp=tp;
			this.fOutputs=fOutputs;
			this.Id=Id;
			this.port=Integer.parseInt(port);
		}
		/*
		 * ����ͻ���������������
		 */
		public void transClientConnect(){
			try {
				ss=new ServerSocket(transPort);
				s=ss.accept();
				ois=new ObjectInputStream(s.getInputStream());
				dis=new DataInputStream(s.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		 * �������������������
		 */
		public void transServerConnect(){
			try {
				s=new Socket(Id,port);
				oos=new ObjectOutputStream(s.getOutputStream());
				dos=new DataOutputStream(s.getOutputStream());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		 * ����ͻ��˽�������
		 */
		public void transClient(File f){
			File fObj=null;
			try {
				fObj=(File)ois.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			/*
			 * ��������
			 */
			if(fObj.getName().lastIndexOf(".")!=-1){
				FileOutputStream fos=null;
				BufferedOutputStream bos=null;
				File newf=new File(f,fObj.getName());
				try {
					fos=new FileOutputStream(newf);
					bos=new BufferedOutputStream(fos);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				try {
					while((len=dis.read(buf))!=-1){
						bos.write(buf,0, len);
					}
					/*
					int b=-1;
					while((b=dis.read())!=-1)
					{
						bos.write(b);
					}*/
					bos.flush();
					fos.close();
					bos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
					
				tp.jl.setText("�ѽ���"+(++fileNum)+"���ļ�");
			}else{
				arrFile=fObj.listFiles();
				new File(f,fObj.getName()).mkdir();
				for(int i=0;i<arrFile.length;i++){
					fObj=new File(f,fObj.getName());
					transClient(fObj);
				}
			}
		}
		/*
		 * �����ļ�����
		 */
		public void launchTrans(){
			for(int i=0;i<fOutputs.length;i++){
				transServer(fOutputs[i]);
			}
			tp.jl.setText("�ܹ�����"+fileNum+"���ļ�");
			/*
			 * �رն˿ں���
			 */
			try {
				s.close();
				oos.close();
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		 * ����������˷�������
		 */
		public void transServer(File fObj){
			try {
				oos.writeObject(fObj);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!fObj.isDirectory()){
				FileInputStream fis=null;
				BufferedInputStream bis=null;
				try {
					fis=new FileInputStream(fObj);
					bis=new BufferedInputStream(fis);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				try {
					while((len=bis.read(buf))!=-1){
						dos.write(buf, 0, len);
					}
System.out.println(fObj.getName());
					/*int b=-1;
					while((b=bis.read())!=-1)
					{
						dos.write(b);
					}*/
					fis.close();
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				tp.jl.setText("�Ѵ���"+(++fileNum)+"���ļ�");
			}else{
				arrFile=fObj.listFiles();
				for(int i=0;i<arrFile.length;i++){
					transServer(new File(fObj,arrFile[i].getName()));
				}
			}
		}
	}
	/*
	 * �����б������
	 */
	class JListMouseAdapter extends MouseAdapter{
		JFrame jf=new JFrame();
		String name="����Ա��Ϣ";
		int no=-1;
		
		JListMouseAdapter(JFrame jf){
			this.jf=jf;
		}
		
		public void mousePressed(MouseEvent me){
			name=((JList)me.getSource()).getSelectedValue().toString();
			
			if(name.equals("Ⱥ��Ϣ")){
				jf.setTitle("�鿴"+name);
				text2.setEnabled(true);
				btn2.setEnabled(true);
			}
			else if(name.equals("����Ա��Ϣ")){
				jf.setTitle("�鿴"+name);
				text2.setEnabled(false);
				btn2.setEnabled(false);
			}
			else{
				if(name.indexOf("*")!=-1){
					name=name.substring(0,name.length()-1);
				}
				no=arrName.indexOf(name);
				jf.setTitle("��"+name+"������....");
				text2.setEnabled(true);
				btn2.setEnabled(true);
				listmod.set(no,name);
			}
			
			text1.setText(lists.get(name).toString());
			text2.setText("");
			
			now_chatObject=name;
		}
	}
	/*
	 * ����崰��ر��¼�������
	 */
	class PaneWindowAdapter extends WindowAdapter{
		
		public void windowClosing(WindowEvent we){
			send("#leave:");
			try {
				dos.close();
				dis.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				System.exit(0);
			}
		}
	}
	/*
	 * �������
	 */
	class TransPane extends JFrame implements ActionListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JLabel jl=new JLabel();
		JButton jbtn=new JButton();
		JButton jbtn2=new JButton();
		String obj="";
		File f=null;
		String name="";
		File []fs=null;
		int len=-1;
		
		TransPane(String name){
			setLayout(null);
			setTitle("��ƽ�߿ͻ��˴��� "+name);
			setResizable(false);
			this.name=name.substring(3);
		}
		/*
		 * ��������
		 */
		public void startupTrans(String Id,String port){
			Trans t=new Trans(this,fs,Id,port);
			t.transServerConnect();
			t.launchTrans();
		}
		/*
		 * ���ƴ�������������
		 */
		public void transServerPane (String ltext,String btext,String obj,File[] fs) {
			this.fs=fs;
			this.obj=obj;
			setBounds(200, 100, 300, 150);
			jl.setBounds(75,23,120,25);
			jbtn.setBounds(110,90 ,120,25);
			jl.setText(ltext);
			jbtn.setText(btext);
			add(jl);
			add(jbtn);
			jbtn.setVisible(false);
			jbtn.addActionListener(this);
			setVisible(true);
		}
		/*
		 * ���ƴ���ͻ������
		 */
		public void transClientPane (String ltext,String b1text,String b2text,String obj,int len) {
			this.len=len;
			this.obj=obj;
			setBounds(200, 100, 300, 150);
			jl.setBounds(100,23,120,25);
			jbtn.setBounds(80,70,60,25);
			jbtn2.setBounds(160,70, 60, 25);
			jl.setText(ltext);
			jbtn.setText(b1text);
			jbtn2.setText(b2text);
			add(jl);
			add(jbtn);
			add(jbtn2);
			jbtn.addActionListener(this);
			jbtn2.addActionListener(this);
			setVisible(true);
		}
		/*
		 * ��������������
		 */
		public void actionPerformed(ActionEvent e) {
			try
			{
				if(obj.equals("�ͻ���")){
					if(e.getSource()==jbtn){
						JFileChooser chooser=new JFileChooser();
						chooser.setMultiSelectionEnabled(false);
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int returVal=chooser.showOpenDialog(jbtn);
						if(returVal==JFileChooser.APPROVE_OPTION){
							f=chooser.getSelectedFile();
							jl.setText("���ڴ���......");
							Trans t=new Trans(this,f);
							send("#transId:"+name+"#"+clientId+"#"+(++transPort));
							t.transClientConnect();
							for(int i=0;i<len;i++){
								t.transClient(f);
							}
							jl.setText("�ļ�������ϣ��ܹ�����"+len+"���ļ�");
						}
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

}
/*==============����Ա���=====================*/
class ServerPane extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<String, ClientThread> clientLists=new HashMap<String, ClientThread>();
	ArrayList<String> arrClientName=new ArrayList<String>();
	DefaultListModel clientListmod = new DefaultListModel();
	ClientThread ct=null;
	String name="";
	String now_chatObject="����Ա��Ϣ";
	int no=-1;
	Time now_Time=null;
	
	StringBuffer serverChat=new StringBuffer("��ӭ��ʹ�õ�ƽ��������������ߣ�������");
	StringBuffer groupChat=new StringBuffer("");
	
	JTextPane text1 = new JTextPane();   //��Ϣ��ʾ��
	JTextPane text2 = new JTextPane();   //��Ϣ�����
	JScrollPane sc1 = new JScrollPane(text1, 22, 32);//��Ϣ��ʾ������
	JScrollPane sc2 = new JScrollPane(text2, 22, 32);//��Ϣ���������
	
	JComboBox cb1 = null;//��������
	JComboBox cb2 = new JComboBox(new String[] { "����", "����", "б��", "��б��" });
	JComboBox cb3 = new JComboBox();//�����С
	
	JList jlist = new JList(clientListmod);//�û��б��
	JScrollPane sc3 = new JScrollPane(jlist, 22, 32);//�û��б��
	
	JButton btn1 = new JButton("�㲥");
	JButton btn2 = new JButton("����");
	JButton btn3 = new JButton("�Ͽ�");
	
	JSplitPane sp0= new JSplitPane(0);//����
	JSplitPane sp1= new JSplitPane(1);//����
	JSplitPane sp2= new JSplitPane(0);//����
	
	Font font = new Font("����", 1, 12);
	Font[] font_array;
	String[] font_name;

	/* =================����Ա��幹�췽��================= */

	ServerPane() {
		/*
		 * �����߳�
		 */
		ClientAddThread cat=new ClientAddThread();
		ReceiveAddThread rat=new ReceiveAddThread();
		cat.start();
		rat.start();
		/*
		 * �������
		 */
		setBounds(100, 200, 500, 500);
		setTitle("��ƽ�����������������");
		GraphicsEnvironment fm = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		font_name = fm.getAvailableFontFamilyNames();
		cb1 = new JComboBox(font_name);
		for (int i = 6; i <= 64; i++){
			cb3.addItem(i);
		}
		
		sc1.setHorizontalScrollBarPolicy(31);
		sc2.setHorizontalScrollBarPolicy(31);
		sc3.setHorizontalScrollBarPolicy(31);
		
		sp0.setTopComponent(sc1);
		sp0.setBottomComponent(sc2);
		sp0.setDividerLocation(280);
		sp1.setLeftComponent(sp0);
		sp1.setRightComponent(sc3);
		sp1.setDividerLocation(370);
		sp2.setTopComponent(sp1);
		
		getContentPane().setBackground(new Color(131, 172, 113));
		JPanel pan2 = new JPanel(new GridLayout(1, 6));
		pan2.add(btn1);
		pan2.add(btn2);
		pan2.add(btn3);
		pan2.add(cb1);
		pan2.add(cb2);
		pan2.add(cb3);
		
		btn1.addActionListener(this);
		btn2.addActionListener(this);
		btn3.addActionListener(this);
		cb1.addActionListener(this);
		cb2.addActionListener(this);
		cb3.addActionListener(this);
		sp2.setBottomComponent(pan2);
		sp2.setDividerLocation(442);
		
		add(sp2, "Center");
		setResizable(false);
		jlist.setSelectedIndex(0);
		sp0.setOneTouchExpandable(true);
		sp1.setOneTouchExpandable(true);
		jlist.addMouseListener(new JListMouseAdapter(this));
		clientListmod.add(clientListmod.size(), "Ⱥ��Ϣ");
		clientListmod.add(clientListmod.size(),"����Ա��Ϣ");
		addWindowListener(new serverPaneWindowAdapter());
		text1.setText("��ӭ��ʹ�õ�ƽ��������������ߣ�������");
	}
	
	/*
	 *����Ա���������¼������� 
	 */
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == btn1) {
			now_Time=new Time(System.currentTimeMillis());
			if(now_chatObject.equals("Ⱥ��Ϣ")){
				groupChat.append("\n\n����Ա:"+text2.getText()+"  "+now_Time.toString());
				text1.setText(groupChat.toString());
				
				for(int i=0;i<clientLists.size();i++){
					ct=clientLists.get(arrClientName.get(i));
					ct.send("#group:"+"����Ա#"+text2.getText()+"  "+now_Time.toString());
				}
			}
			else{
				serverChat.append("\n\n"+text2.getText()+"  "+now_Time.toString());
				text1.setText(serverChat.toString());
				
				for(int i=0;i<clientLists.size();i++){
					ct=clientLists.get(arrClientName.get(i));
					ct.send("#server:"+text2.getText()+"  "+now_Time.toString());
				}
			}
			
			text2.setText("");
		} else if (o == btn2) {
			/////////////////////////////////////////////////////////////////	
		} else if(o == btn3){
			name=clientListmod.get(no).toString();
			if(!name.equals("")&&!name.equals("Ⱥ��Ϣ")&&!name.equals("����Ա��Ϣ")){
				if(btn3.getText().equals("����")){
					clientListmod.set(no,arrClientName.get(no-2));
					btn3.setText("�Ͽ�");
				}else{
					clientListmod.set(no, name+"*");
					btn3.setText("����");
				}
			}
			
		}else if (o == cb1 || o == cb2 || o == cb3) {
			String fontname = String.valueOf(cb1.getSelectedItem());
			int style = cb2.getSelectedIndex();
			int size = 6 + cb3.getSelectedIndex();
			font = new Font(fontname, style, size);
			text1.setFont(font);
			text2.setFont(font);
		}
	}
	/*
	 * �����¿ͻ���
	 */
	class ClientAddThread extends Thread{
		byte [] buf=new byte[1024];
		byte [] buf2=new byte[1024];
		DatagramPacket dp=new DatagramPacket(buf,buf.length);
		DatagramPacket dp2=null;
		DatagramSocket dgs=null;
		DatagramSocket dgs2=null;
		String clientIns="";
		String name="";
		String clientId="";
		int start=-1;
		String id="";
		
		public void run (){
			try{
				id=InetAddress.getLocalHost().toString();
				id=id.substring(id.lastIndexOf("/")+1);
			}catch(UnknownHostException uhe){
				System.out.println("���ط�������ַ��ȡʧ��");
				uhe.printStackTrace();
			}
			
			try{
				dgs=new DatagramSocket(3000);
				dgs2=new DatagramSocket(3030);
			}catch(IOException ie){
				System.out.println("�������˼������û��Ķ˿ڿ���ʧ��");
				ie.printStackTrace();
			}
			
			while(true){
				try {
					dgs.receive(dp);
				} catch (IOException e) {
					System.out.println("������ͨ���޷�������Ϣ");
					e.printStackTrace();
				}
				
				clientIns=new String(buf,0,dp.getLength());
				start=clientIns.indexOf("/");
				name=clientIns.substring(0,start);
				start=clientIns.lastIndexOf("/");
				clientId=clientIns.substring(start+1);
				if(!arrClientName.contains(name)){
					buf2=id.getBytes();
				
					try {
						dp2=new DatagramPacket(buf2,buf2.length,new InetSocketAddress(clientId,2020));
						dgs2.send(dp2);
					} catch (IOException ie) {
						System.out.println("ͨ��δ����");
						ie.printStackTrace();
					}
				}else{
					buf2=new String("#repeat:").getBytes();
					
					try{
						dp2=new DatagramPacket(buf2,buf2.length,new InetSocketAddress(clientId,2020));
						dgs2.send(dp2);
					}catch(IOException ie){
						System.out.println("�����޷�����");
						ie.printStackTrace();
					}
				}
			}
		}
	}
	/*
	 * ���ܿͻ��˼���
	 */
	class ReceiveAddThread extends Thread{
		ServerSocket ss =null;
		Socket s=null;
		DataInputStream dis=null;
		DataOutputStream dos=null;
		String name="";
		int num=-1;
		ClientThread ct=null;
		
		public void run(){
			try {
				ss=new ServerSocket(3333);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			while (true) {
				try {
					s=ss.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					dis =new DataInputStream(s.getInputStream());
					name=dis.readUTF();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					dos=new DataOutputStream(s.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				for (int i = 0; i < arrClientName.size(); i++) {
					try {
						dos.writeUTF("#add:"+arrClientName.get(i));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				for(int i=0;i<clientLists.size();i++){
					ct=clientLists.get(arrClientName.get(i));
					ct.send("#add:"+name);
				}
				
				num=arrClientName.size();
				ClientThread ct=new ClientThread(s,name);
				arrClientName.add(name);
				clientLists.put(name, ct);
				clientListmod.add(num+2,name );
				ct.start();
			}
		}
	}
	/*
	 * �ͻ����߳̽ӷ���Ϣ
	 */
	class ClientThread extends Thread{
		Socket s=null;
		String ins="";
		DataOutputStream dos=null;
		DataInputStream dis=null;
		ClientThread ct=null;
		String myName="";
		int num=-1;
		
		public ClientThread(Socket s,String myName) {
			this.s=s;
			this.myName=myName;
			
			try {
				dos=new DataOutputStream(s.getOutputStream());
				dis=new DataInputStream(s.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/*
		 * ������Ϣ
		 */
		public void run(){
			String name="";
			int end=-1;
			String Id="";
			
			while(true){
				try {		
					ins=dis.readUTF();
				} catch (IOException e) {
					e.printStackTrace();
					ins="#leave:";
				}
				num=arrClientName.indexOf(myName);
				if(clientListmod.get(num+2).toString().indexOf("*")!=-1){
					continue;
				}
				
				if(ins.indexOf("#name:")!=-1){
					end=ins.indexOf("#",6);
					name=ins.substring(6,end);
					ins=ins.substring(end+1);
					ct=clientLists.get(name);
					ct.send("#name:"+myName+"#"+ins);
				}
				
				if(ins.indexOf("#group:")!=-1){
					ins=ins.substring(7);
					groupChat.append("\n\n"+myName+"˵��"+ins);
					if(now_chatObject.equals("Ⱥ��Ϣ")){
						text1.setText(groupChat.toString());
					}
					
					for (int i = 0; i <clientLists.size(); i++) {
						if(i==num){
							continue;
						}
						ct=clientLists.get(arrClientName.get(i));
						ct.send("#group:"+myName+"#"+ins);
					}
				}
				
				if(ins.indexOf("#leave:")!=-1){
					clientLists.remove(myName);
					clientListmod.remove(num+2);
					arrClientName.remove(myName);
					for(int i=0;i<clientLists.size();i++){
						ct=clientLists.get(arrClientName.get(i));
						ct.send("#leave:"+myName);
					}
					break;
				}
				
				if(ins.indexOf("#trans:")!=-1){
					name=ins.substring(7,ins.lastIndexOf("#"));
					ct=clientLists.get(name);
					ct.send("#trans:"+myName+"#"+ins.substring(ins.lastIndexOf("#")+1));
				}
				
				if(ins.indexOf("#transId:")!=-1){
					name=ins.substring(9,ins.indexOf("#",9));
					Id=ins.substring(ins.indexOf("#",9));
					ct=clientLists.get(name);
					ct.send("#transId:"+myName+Id);
				}
			}
			
			try {
				dos.close();
				dis.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		/*
		 * ������Ϣ
		 */
		public void send(String ins){
			try {
				dos.writeUTF(ins);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/*
	 * �����б������
	 */
	class JListMouseAdapter extends MouseAdapter{
		JFrame jf=new JFrame();
		
		JListMouseAdapter(JFrame jf){
			this.jf=jf;
		}
		
		public void mousePressed(MouseEvent me){
			name=((JList)me.getSource()).getSelectedValue().toString();
			no=((JList)me.getSource()).getSelectedIndex();
			if(name.equalsIgnoreCase("Ⱥ��Ϣ")){
				jf.setTitle("�鿴"+name);
				text1.setText(groupChat.toString());
				now_chatObject="Ⱥ��Ϣ";
				btn3.setEnabled(false);
			}
			else if(name.equalsIgnoreCase("����Ա��Ϣ")){
				jf.setTitle("�鿴"+name);
				text1.setText(serverChat.toString());
				now_chatObject="����Ա��Ϣ";
				btn3.setEnabled(false);
			}
			else{
				btn3.setEnabled(true);
				if(name.indexOf("*")!=-1){
					btn3.setText("����");/////////////////////////////////////////////////////////////////////////////////
				}else{
					btn3.setText("�Ͽ�");
				}
			}
		}
	}
	/*
	 * ����Ա��崰��ر��¼�������
	 */
	class serverPaneWindowAdapter extends WindowAdapter{
		ClientThread ct=null;
		
		public void windowClosing(WindowEvent we){
			if(clientLists.size()>=1){
				for(int i=0;i<clientLists.size();i++){
					ct=clientLists.get(arrClientName.get(i));
					ct.send("#serverClosed:");
				}
			}
			System.exit(0);
		}
	}
}
