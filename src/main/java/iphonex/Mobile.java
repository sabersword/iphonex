package iphonex;


import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Mobile extends JFrame{
	public static JLabel labelSendMsg,labelLogin,labelBuy,labelSave,labelStock;
	public static JTable table;
	public static JButton btnLoad,btnSendMsg,btnLogin,btnBuy,btnSave;
	private JTextField inputMaxSendMsgReq,inputMaxLoginReq,inputMaxBuyReq,inputProdID,inputModelID;
	public  volatile static String localPath;
	public static FileWriter fileWriterLogin,fileWriterGuess;
	public static BufferedWriter bufferWriterLogin,bufferWriterGuess;
	public static Object lock=new Object();//表格锁
	public static Object lockFile=new Object();//文件锁
	public ReqManager reqManager;
	private String selectedFileName;	
	private static DefaultTableModel tableModel;
	private Vector<String> columnVector;
	public static void main(String arg[]){
		Mobile mobile=new Mobile();
		mobile.setVisible(true);
		
	}	
	public Mobile(){
		super();
		initUI();
//        createLogFile();
	}

	private void createLogFile(){
        String path = Mobile.class.getResource("/").toString();
        path = path.replace("file:/", "");
        localPath = path;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = df.format(new Date());
            path += "log" + currentTime + ".txt";
            fileWriterLogin = new FileWriter(path);
            bufferWriterLogin = new BufferedWriter(fileWriterLogin);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	private void initUI(){
		setTitle("手机活动助手");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //�õ���Ļ�ĳߴ� 
		int width=(int)screenSize.getWidth();
		
		setBounds(width/2-325,100,650,400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		//getContentPane().add(label,BorderLayout.NORTH);

		btnLoad=new JButton();
		btnLoad.setBounds(40, 0, 90, 25);
		btnLoad.setText("导入帐号");
		btnLoad.setFont(new Font("微软雅黑",Font.PLAIN,14));
        btnSendMsg=new JButton();
        btnSendMsg.setBounds(40, 25, 90, 25);
        btnSendMsg.setText("发验证码");
        btnSendMsg.setFont(new Font("微软雅黑",Font.PLAIN,14));
		btnLogin=new JButton();
		btnLogin.setBounds(40, 50, 90, 25);
		btnLogin.setText("登录");
		btnLogin.setFont(new Font("微软雅黑",Font.PLAIN,14));
		btnBuy=new JButton();
		btnBuy.setBounds(40, 75, 90, 25);
		btnBuy.setFont(new Font("微软雅黑",Font.PLAIN,14));
		btnBuy.setText("购买");
		
		inputMaxLoginReq=new JTextField();
		inputMaxSendMsgReq=new JTextField();
		inputMaxBuyReq=new JTextField();
        inputMaxSendMsgReq.setBounds(40, 160, 90, 20);
		inputMaxLoginReq.setBounds(40, 180, 90, 20);
		inputMaxBuyReq.setBounds(40, 200, 90, 20);
        inputMaxSendMsgReq.setText("30");
        inputMaxSendMsgReq.setHorizontalAlignment(JTextField.CENTER);
		inputMaxLoginReq.setText("30");
		inputMaxLoginReq.setHorizontalAlignment(JTextField.CENTER);
		inputMaxBuyReq.setText("30");
		inputMaxBuyReq.setHorizontalAlignment(JTextField.CENTER);

        labelSendMsg = new JLabel();
        labelSendMsg.setText("0/0");
        labelSendMsg.setHorizontalAlignment(JLabel.CENTER);
        labelSendMsg.setFont(new Font("微软雅黑",Font.PLAIN,12));
        labelSendMsg.setBounds(40, 100, 90, 20);
		labelLogin = new JLabel();
		labelLogin.setText("0/0");
		labelLogin.setHorizontalAlignment(JLabel.CENTER);
		labelLogin.setFont(new Font("微软雅黑",Font.PLAIN,12));
		labelLogin.setBounds(40, 120, 90, 20);
		labelBuy = new JLabel();
		labelBuy.setText("0/0");
		labelBuy.setHorizontalAlignment(JLabel.CENTER);
		labelBuy.setFont(new Font("微软雅黑",Font.PLAIN,12));
		labelBuy.setBounds(40, 140, 90, 20);
		
		btnSave=new JButton();
		btnSave.setBounds(40, 225, 90, 25);
		btnSave.setFont(new Font("微软雅黑",Font.PLAIN,14));
		btnSave.setText("保存结果");

		labelSave=new JLabel();
		labelSave.setText("");
		labelSave.setHorizontalAlignment(JLabel.CENTER);
		labelSave.setFont(new Font("微软雅黑",Font.PLAIN,12));
		labelSave.setBounds(40, 250, 90, 20);
		
		inputProdID=new JTextField();
		inputProdID.setBounds(40, 270, 90, 20);
		inputProdID.setHorizontalAlignment(JTextField.CENTER);
		inputProdID.setText("1022546");
		
		inputModelID=new JTextField();
		inputModelID.setBounds(40, 290, 90, 20);
		inputModelID.setHorizontalAlignment(JTextField.CENTER);
		inputModelID.setText("1015922");
		
		labelStock=new JLabel();
		labelStock.setText("库存");
		labelStock.setHorizontalAlignment(JLabel.CENTER);
		labelStock.setFont(new Font("微软雅黑",Font.PLAIN,12));
		labelStock.setBounds(40, 310, 90, 20);
		
		JPanel panel=new JPanel();		
		panel.setLayout(null);
		panel.add(btnLogin,null);
		panel.add(btnLoad);
		panel.add(btnSendMsg,null);
		panel.add(btnLogin,null);
		panel.add(btnBuy,null);
		panel.add(inputMaxSendMsgReq,null);
		panel.add(inputMaxLoginReq,null);
		panel.add(inputMaxBuyReq,null);
		panel.add(labelSendMsg,null);
		panel.add(labelLogin,null);
		panel.add(labelBuy,null);
		panel.add(btnSave,null);
		panel.add(labelSave,null);
		panel.add(inputProdID,null);
		panel.add(inputModelID,null);
		panel.add(labelStock,null);
	
		tableModel=new DefaultTableModel();
		columnVector=new Vector();
		columnVector.add("序号");
		columnVector.add("帐号");
		columnVector.add("密文");
		columnVector.add("状态");
		Vector dataVector=new Vector();
		for(int i=0;i<11;i++){
			Vector<String> rowVector=new Vector();	
			rowVector.add("");
			rowVector.add("");
			rowVector.add("");
			rowVector.add("");
			dataVector.add(rowVector);
		}	
		table=new JTable(tableModel);
		tableModel.setDataVector(dataVector, columnVector);		
		table.setRowHeight(20);
		table.setBounds(0, 0, 300, 200);			
		JScrollPane scrollPane=new JScrollPane();
		scrollPane.setViewportView(table);

		Container c=getContentPane();
		c.setLayout(new BorderLayout());
		c.add(scrollPane,BorderLayout.WEST);
		c.add(panel,BorderLayout.CENTER);
	
		btnLoad.addMouseListener(new MouseAdapter(){
			public void mouseClicked(final MouseEvent arg0){
				if(btnLoadClicked())btnLoad.setEnabled(false);
			}
		});
        btnSendMsg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int maxReqNum = Integer.valueOf(inputMaxSendMsgReq.getText().trim());
                reqManager.startSendMsg(maxReqNum);
                btnSendMsg.setEnabled(false);
                inputMaxLoginReq.setEditable(false);

            }
        });
		btnLogin.addMouseListener(new MouseAdapter(){
			public void mouseClicked(final MouseEvent arg0){
                int maxReqNum = Integer.valueOf(inputMaxLoginReq.getText().trim());
				reqManager.startLogin(maxReqNum);
				btnLogin.setEnabled(false);
                inputMaxLoginReq.setEditable(false);
			}
		});
		btnBuy.addMouseListener(new MouseAdapter(){
			public void mouseClicked(final MouseEvent arg0){
                int maxReqNum = Integer.valueOf(inputMaxBuyReq.getText().trim());
                int prodID = Integer.valueOf(inputProdID.getText().trim());
                int modelID = Integer.valueOf(inputModelID.getText().trim());
                reqManager.startBuy(maxReqNum, prodID, modelID);
                btnBuy.setEnabled(false);
                inputMaxBuyReq.setEditable(false);

            }
		});
        inputMaxSendMsgReq.addFocusListener(new FocusListener(){
            @Override
            public void focusGained(FocusEvent e) {
                // TODO Auto-generated method stub
            }
            @Override
            public void focusLost(FocusEvent e) {
                // TODO Auto-generated method stub
                String value=inputMaxSendMsgReq.getText().trim();
                if(value.equals("")){
                    inputMaxSendMsgReq.setText("30");
                    return;
                }
                if(Integer.valueOf(value) < 0){
                    inputMaxSendMsgReq.setText("30");
                }
            }
        });
		inputMaxLoginReq.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
			// TODO Auto-generated method stub
			}
			@Override
			public void focusLost(FocusEvent e) {
			// TODO Auto-generated method stub
				String value=inputMaxLoginReq.getText().trim();				
				if(value.equals("")){
					inputMaxLoginReq.setText("30");
					return;
				}
				if(Integer.valueOf(value) < 0){
					inputMaxLoginReq.setText("30");
				}
			}
		});
		inputMaxBuyReq.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
			// TODO Auto-generated method stub
			}
			@Override
			public void focusLost(FocusEvent e) {
			// TODO Auto-generated method stub
				String value=inputMaxBuyReq.getText().trim();				
				if(value.equals("")){
					inputMaxBuyReq.setText("30");
					return;
				}
				if(Integer.valueOf(value)<0){
					inputMaxBuyReq.setText("30");
				}
			}
		});
		btnSave.addMouseListener(new MouseAdapter(){
			public void mouseClicked(final MouseEvent arg0){
				btnSaveClicked();
			}
		});
		
	}
	public void updateTableState(int id, String status){
	    System.out.println("table: "+status);
	    this.table.setValueAt(status, id, 3);
    }
    public void updateSendMsgState(int curSucNum, int curReqNum){
	    String state = String.valueOf(curSucNum) + "/" + String.valueOf(curReqNum);
        labelSendMsg.setText(state);
    }
    public void updateLoginState(int curSucNum, int curReqNum){
	    String state = String.valueOf(curSucNum) + "/" + String.valueOf(curReqNum);
        labelLogin.setText(state);
    }
    public void updateBuyState(int curSucNum, int curReqNum){
        String state = String.valueOf(curSucNum) + "/" + String.valueOf(curReqNum);
        labelBuy.setText(state);
    }
	private boolean btnLoadClicked(){
		JFileChooser fileChooser=new JFileChooser(localPath);
		FileNameExtensionFilter filter=new FileNameExtensionFilter("文本文件","txt");
		fileChooser.setFileFilter(filter);
		fileChooser.setDialogTitle("选择文件");
		fileChooser.setFont(new Font("宋体",Font.PLAIN,20));
		int returnVal=fileChooser.showOpenDialog(null);
		if(returnVal == JFileChooser.CANCEL_OPTION){ //APPROVE_OPTION 
            return false;  
        }  
		String fileName=fileChooser.getSelectedFile().getAbsolutePath();	
		selectedFileName=fileName;
		if(fileName!=""){
			try {				
				loadAccountInfo(fileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("error");
				e.printStackTrace();
			}
		}
		
		if(this.reqManager.iphonexVec.size() == 0)return false;
        Vector dataVector = new Vector();
        for(int i = 0; i<this.reqManager.iphonexVec.size(); i++){
			Vector rowVector=new Vector();
			rowVector.add(String.valueOf(i));
			rowVector.add(this.reqManager.iphonexVec.get(i).getCellNum());
			rowVector.add(this.reqManager.iphonexVec.get(i).getCellNumEnc());
			rowVector.add("...");
			dataVector.add(rowVector);
		}
		
		tableModel.setDataVector(dataVector, columnVector);
		//设置第一列对齐方式
		DefaultTableCellRenderer d = new DefaultTableCellRenderer(); 
		d.setHorizontalAlignment(JLabel.CENTER); 
		TableColumn col = table.getColumn(table.getColumnName(0)); 
		col.setCellRenderer(d); 
		return true;
	}

	private void btnSaveClicked(){
		try {	
			int pos=selectedFileName.lastIndexOf("\\");
			if(pos==-1)return;
			String selectedPath=selectedFileName.substring(0, pos+1);
			JFileChooser fileChooser=new JFileChooser(selectedPath);
			FileNameExtensionFilter filter=new FileNameExtensionFilter("文本文件","txt");
			fileChooser.setFileFilter(filter);
			fileChooser.setDialogTitle("保存");
			fileChooser.setFont(new Font("微软雅黑",Font.PLAIN,14));
			File fileSelected=new File(selectedFileName);
			fileChooser.setSelectedFile(fileSelected);
			int returnVal=fileChooser.showSaveDialog(null);
			if(returnVal == JFileChooser.CANCEL_OPTION){  
	            return ;  
	        }  
			fileWriterLogin.close();
			fileWriterGuess.close();
			bufferWriterLogin.close();
			bufferWriterGuess.close();
			String fileName=fileChooser.getSelectedFile().getAbsolutePath();
			if(fileName.indexOf(".txt") == -1){
				labelSave.setText("文件格式错误");
				return;
			}
			File fileGuessTmp=new File(localPath + "登录临时文件.txt");
			File fileGuess=new File(fileName);
			FileInputStream fileStreamGuessTmp=new FileInputStream(fileGuessTmp);
			FileOutputStream fileStreamGuess=new FileOutputStream(fileGuess);
			byte b[]=new byte[(int) fileGuessTmp.length()];
			fileStreamGuessTmp.read(b);
			fileStreamGuess.write(b);	
			labelSave.setText("保存成功！");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private void loadAccountInfo(String filename) throws IOException{
	    try {
            File srcFile = new File(filename);
            FileReader ins;
            ins = new FileReader(srcFile);
            BufferedReader readBuf = new BufferedReader(ins);
            String s;
            reqManager = new ReqManager(this);
            int id = 0;
            while ((s = readBuf.readLine()) != null) {
                s = s.trim();
                if (s.equals("") || s.equals(null) || s.length() < 10) continue;
                int pos = s.indexOf("-");
                System.out.println(s);
                String cellNum = s.substring(0, pos);
                String cellNumEnc = s.substring(pos + 4);
                IPhoneX iPhoneX = new IPhoneX(reqManager, id, cellNum, cellNumEnc);
                reqManager.iphonexVec.add(iPhoneX);
                id += 1;
            }
            readBuf.close();
        }catch (Exception e){
	        System.out.println("加载帐号错误，请检查格式");
        }
	}

}
