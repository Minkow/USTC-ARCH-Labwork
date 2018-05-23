import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class directory {

	static int state[] = new int[16];
	static int padd[] = new int[16];
	static int mstate[] = new int[40];
	static boolean wb = false;
	static boolean share[][] = new boolean[5][40];
	static int source = 0;
	static int bus=0;

	/*****创建panel2~panel5******/
	static Mypanel panel2 =new Mypanel();
	static Mypanel panel3 =new Mypanel();
	static Mypanel panel4 =new Mypanel();
	static Mypanel panel5 =new Mypanel();

	static JComboBox<String> Mylistmodel1 = new JComboBox<>(new listmodel());
	static class listmodel extends AbstractListModel<String> implements ComboBoxModel<String>{
		private static final long serialVersionUID = 1L;
		String selecteditem=null;
		private String[] test={"直接映射","两路组相联","四路组相联"};
		public String getElementAt(int index){
			return test[index];
		}
		public int getSize(){
			return test.length;
		}
		public void setSelectedItem(Object item){
			selecteditem=(String)item;
		}
		public Object getSelectedItem( ){
			return selecteditem;
		}
		public int getIndex() {
			for (int i = 0; i < test.length; i++) {
				if (test[i].equals(getSelectedItem()))
					return i;
			}
			return 0;
		}

	}
	static class listmodel2 extends AbstractListModel<String> implements ComboBoxModel<String>{
		private static final long serialVersionUID = 1L;
		String selecteditem=null;
		private String[] test={"读","写"};
		public String getElementAt(int index){
			return test[index];
		}
		public int getSize(){
			return test.length;
		}
		public void setSelectedItem(Object item){
			selecteditem=(String)item;
		}
		public Object getSelectedItem( ){
			return selecteditem;
		}
		public int getIndex() {
			for (int i = 0; i < test.length; i++) {
				if (test[i].equals(getSelectedItem()))
					return i;
			}
			return 0;
		}

	}

	static class Mypanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;
		JLabel label=new JLabel("访问地址");
		JLabel label_2=new JLabel("Process1");

		JTextField jtext=new JTextField("");
		JButton button=new JButton("执行");
		JComboBox<String> Mylistmodel = new JComboBox<>(new listmodel2());


		/*********cache中的标题*********/
		String[] Cache_ca={"Cache","读/写","目标地址"};
		/*********cache中的内容*********/
		String[][] Cache_Content = {
				{"0"," "," "},{"1"," "," "},{"2"," "," "},{"3"," "," "}
		};
		/*********memory的标题*********/
		String[] Mem_ca={
				"Memory","",""
		};

		/*********memory中的内容*********/
		String[][] Mem_Content ={
				{"0","",""},{"1","",""},{"2","",""},{"3","",""},{"4","",""},{"5","",""},{"6","",""},{"7","",""},
				{"8","",""},{"9","",""}
		};
		/************cache的滚动模版***********/
		JTable table_1 = new JTable(Cache_Content,Cache_ca);
		JScrollPane scrollPane = new JScrollPane(table_1);
		/************memory的滚动模版***********/
		JTable table_2 = new JTable(Mem_Content,Mem_ca);
		JScrollPane scrollPane2 = new JScrollPane(table_2);

		public Mypanel(){
			super();
			setSize(350, 400);
			setLayout(null);

			/*****添加原件********/
			add(jtext);
			add(label);
			add(label_2);
			add(button);
			add(Mylistmodel);
			add(scrollPane);
			add(scrollPane2);

			/****设置原件大小与字体********/
			label_2.setFont(new Font("",1,16));
			label_2.setBounds(10, 10, 100, 30);

			label.setFont(new Font("",1,16));
			label.setBounds(10, 50, 100, 30);

			jtext.setFont(new Font("",1,15));
			jtext.setBounds(100, 50, 50, 30);

			Mylistmodel.setFont(new Font("",1,15));
			Mylistmodel.setBounds(160, 50, 50, 30);

			scrollPane.setFont(new Font("",1,15));
			scrollPane.setBounds(10, 90, 310, 90);

			scrollPane2.setFont(new Font("",1,15));
			scrollPane2.setBounds(10, 190, 310, 185);

			button.setFont(new Font("",1,15));
			button.setBounds(220,50, 100, 35);

			/******添加按钮事件********/
			button.addActionListener(this);
		}

		public void init(){
			/******Mypanel的初始化******/
			jtext.setText("");
			Mylistmodel.setSelectedItem(null);
			for(int i=0;i<=3;i++)
				for(int j=1;j<=2;j++)
					Cache_Content[i][j]=" ";
			for(int i=0;i<=9;i++)
				for(int j=1;j<=2;j++)
					Mem_Content[i][j]=" ";
			for(int i=0; i<16; i++)
			{
				state[i]=0;
				padd[i]=-1;
			}
			for(int i=0; i<5; i++)
				for(int j=0; j<40 ;j++)
					share[i][j] = false;
			for(int i=0; i<40; i++)
				mstate[i] = 0;
			setVisible(false);
			setVisible(true);

		}


		public int setcache(int addr, int process, int ways)
		{
			int p=0;
			if(ways == 0)
			{
				p = (addr % 4) + process * 4;
				if(padd[p] != -1 )
				{
					share[process+1][padd[p]] = false;
				}

			}
			else if(ways == 1)
			{
				p =(addr % 2) + process * 4;
				if(padd[p]!=-1)
				{
					if(padd[p+2]!=1)
					{
						Random r = new Random();
						int a = r.nextInt(2);
						p = (addr % 2) + process * 4 + 2*a;
						share[process+1][padd[p]] = false;
					}
					else
					{
						p = (addr % 2) + process * 4 + 2;
					}
				}
			}
			else if(ways == 2)
			{
				boolean flag = false;
				for(int i=0; i<4; i++)
				{
					if(padd[process * 4 + i] == -1)
					{
						p=i;
						flag = true;
						break;
					}
				}
				if(!flag)
				{
					Random r = new Random();
					int a = r.nextInt(4);
					p = a + process * 4;
					share[process+1][padd[p]] = false;
				}
			}
			return p;
		}


		public void actionPerformed(ActionEvent e){
			/******编写自己的处理函数*******/
			String temp;
			int addr=0;
			int wr=0;
			int ways = Mylistmodel1.getSelectedIndex();
			int process=0;

//			System.out.println(""+ways);

			if(e.getSource() == panel2.button)
			{
				temp = panel2.jtext.getText();
				addr = Integer.parseInt(temp);
				wr = panel2.Mylistmodel.getSelectedIndex();
				process = 0;
			}

			else if(e.getSource() == panel3.button)
			{
				temp = panel3.jtext.getText();
				addr = Integer.parseInt(temp);
				wr = panel3.Mylistmodel.getSelectedIndex();
				process = 1;
			}

			else if(e.getSource() == panel4.button)
			{
				temp = panel4.jtext.getText();
				addr = Integer.parseInt(temp);
				wr = panel4.Mylistmodel.getSelectedIndex();
				process = 2;
			}

			else if(e.getSource() == panel5.button)
			{
				temp = panel5.jtext.getText();
				addr = Integer.parseInt(temp);
				wr = panel5.Mylistmodel.getSelectedIndex();
				process = 3;
			}

//			System.out.println(""+addr);
//			System.out.println(""+wr);


			int p = setcache(addr,process,ways);
			source = p / 4 + 1;
			switch (state[p])
			{
				case 0:
				{
					if (wr == 0)
					{
						padd[p] = addr;
						bus = 1;
						state[p] = 1;
					}
					else if (wr == 1)
					{
						padd[p] = addr;
						bus = 2;
						state[p] = 2;
					}
					break;
				}

				case 1:
				{
					if(wr == 0)
					{
						padd[p] = addr;
					}
					else if(wr == 1)
					{
						if(addr == padd[p])
						{
							bus = 3;
							state[p] = 2;
						}
						else
						{
							bus = 2;
							state[p] = 2;
						}
					}
					break;
				}
				case 2:
				{
					if(padd[p] != addr)
					{
						if(wr == 0)
						{
							bus = 1;
							padd[p] = addr;
							state[p] = 1;
						}
						else if(wr == 1)
						{
							padd[p] = addr;
							state[p] = 2;
							bus = 2;
						}
					}
					break;
				}
			}

			for(int i=0;i<16;i++)
			{
				if(padd[i] == addr && i!=p)
				{
					if(state[i] == 1 && (bus == 2 || bus == 3))
					{
						state[i] = 0;
						padd[i] = -1;
					}
					else if(state[i] == 2 && bus == 1)
					{
						state[i] = 1;
					}
					else if(state[i] == 2 && bus == 2)
					{
						state[i] = 0;
						padd[i] = -1;
					}
				}
			}

			if(bus == 1)
			{
				share[source][addr] = true;
			}
			else if(bus == 2 || bus == 3)
			{
				for(int i=0; i<5; i++)
				{
					share[i][addr] = false;
				}
				share[source][addr] = true;
			}

			for(int i=0; i<4; i++)
			{
				if(padd[i]>=0)
				{
					panel2.Cache_Content[i][2] = ""+padd[i];
				}
				else
				{
					panel2.Cache_Content[i][2] = " ";
				}
				switch(state[i])
				{
					case 0:panel2.Cache_Content[i][1] = "失效";break;
					case 1:panel2.Cache_Content[i][1] = "共享";break;
					case 2:panel2.Cache_Content[i][1] = "独占";break;
				}
			}
			for(int i=4; i<8; i++)
			{
				if(padd[i]>=0)
				{
					panel3.Cache_Content[i-4][2] = ""+padd[i];
				}
				else
				{
					panel3.Cache_Content[i-4][2] = " ";
				}
				switch(state[i])
				{
					case 0:panel3.Cache_Content[i-4][1] = "失效";break;
					case 1:panel3.Cache_Content[i-4][1] = "共享";break;
					case 2:panel3.Cache_Content[i-4][1] = "独占";break;
				}
			}
			for(int i=8; i<12; i++)
			{
				if(padd[i]>=0)
				{
					panel4.Cache_Content[i-8][2] = ""+padd[i];
				}
				else
				{
					panel4.Cache_Content[i-8][2] = " ";
				}
				switch(state[i])
				{
					case 0:panel4.Cache_Content[i-8][1] = "失效";break;
					case 1:panel4.Cache_Content[i-8][1] = "共享";break;
					case 2:panel4.Cache_Content[i-8][1] = "独占";break;
				}
			}
			for(int i=12; i<16; i++)
			{
				if(padd[i]>=0)
				{
					panel5.Cache_Content[i-12][2] = ""+padd[i];
				}
				else
				{
					panel5.Cache_Content[i-12][2] = " ";
				}
				switch(state[i])
				{
					case 0:panel5.Cache_Content[i-12][1] = "失效";break;
					case 1:panel5.Cache_Content[i-12][1] = "共享";break;
					case 2:panel5.Cache_Content[i-12][1] = "独占";break;
				}
			}

			for(int i=0; i<10; i++)
			{
				String out = "";
				for(int j=0; j<5; j++)
				{
					if(share[j][i] == true)
					{
						out += j;
					}
				}
				panel2.Mem_Content[i][1] = out;
			}

			for(int i=10; i<20; i++)
			{
				String out = "";
				for(int j=0; j<5; j++)
				{
					if(share[j][i] == true)
					{
						out += j;
					}
				}
				panel3.Mem_Content[i-10][1] = out;
			}

			for(int i=20; i<30; i++)
			{
				String out = "";
				for(int j=0; j<5; j++)
				{
					if(share[j][i] == true)
					{
						out += j;
					}
				}
				panel4.Mem_Content[i-20][1] = out;
			}

			for(int i=30; i<40; i++)
			{
				String out = "";
				for(int j=0; j<5; j++)
				{
					if(share[j][i] == true)
					{
						out += j;
					}
				}
				panel5.Mem_Content[i-30][1] = out;
			}

			/**********显示刷新后的数据********/
			panel2.setVisible(false);
			panel2.setVisible(true);
			panel3.setVisible(false);
			panel3.setVisible(true);
			panel4.setVisible(false);
			panel4.setVisible(true);
			panel5.setVisible(false);
			panel5.setVisible(true);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame myjf = new JFrame("多cache一致性模拟之目录法");
		myjf.setSize(1500, 600);
		myjf.setLayout(null);
		myjf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container C1 = myjf.getContentPane();

		/*****新建panel1*****/
		JPanel panel1 = new JPanel();

		C1.add(panel2);
		C1.add(panel3);
		C1.add(panel4);
		C1.add(panel5);
		panel2.setBounds(10, 100, 350, 400);
		panel3.setBounds(360, 100, 350, 400);
		panel4.setBounds(720, 100, 350, 400);
		panel5.setBounds(1080, 100, 350, 400);


		/********设置每个Mypanel的不同的参数************/
		panel2.label_2.setText("Process1");
		panel3.label_2.setText("Process2");
		panel4.label_2.setText("Process3");
		panel5.label_2.setText("Process4");
		panel2.table_1.getColumnModel().getColumn(0).setHeaderValue("cache1");
		panel2.Cache_ca[0]="Cache1";
		panel3.table_1.getColumnModel().getColumn(0).setHeaderValue("cache2");
		panel3.Cache_ca[0]="Cache2";
		panel4.table_1.getColumnModel().getColumn(0).setHeaderValue("cache3");
		panel4.Cache_ca[0]="Cache3";
		panel5.table_1.getColumnModel().getColumn(0).setHeaderValue("cache4");
		panel5.Cache_ca[0]="Cache4";


		panel2.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory1");
		panel3.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory2");
		panel4.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory3");
		panel5.table_2.getColumnModel().getColumn(0).setHeaderValue("Memory4");

		for(int i=0;i<10;i++){
			panel3.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel3.Mem_Content[i][0])+10));
			panel4.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel3.Mem_Content[i][0])+10));
			panel5.Mem_Content[i][0]=String.valueOf((Integer.parseInt(panel3.Mem_Content[i][0])+20));
		}
		/********设置头部panel*****/
		panel1.setBounds(10, 10, 1500, 100);
		panel1.setLayout(null);

		JLabel label1_1=new JLabel("执行方式:单步执行");
		label1_1.setFont(new Font("",1,20));
		label1_1.setBounds(15, 15, 200, 40);
		panel1.add(label1_1);

		//JComboBox<String> Mylistmodel1_1 = new JComboBox<>(new Mylistmodel());
		Mylistmodel1.setBounds(220, 15, 150, 40);
		Mylistmodel1.setFont(new Font("",1,20));
		panel1.add(Mylistmodel1);

		JButton button1_1=new JButton("复位");
		button1_1.setBounds(400, 15, 70, 40);

		panel2.init();
		panel3.init();
		panel4.init();
		panel5.init();

		/**********复位按钮事件（初始化）***********/
		button1_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				panel2.init();
				panel3.init();
				panel4.init();
				panel5.init();
				Mylistmodel1.setSelectedItem(null);

			}
		});

		/*panel2.Mem_Content[1][1]="11";*/
		panel1.add(button1_1);
		C1.add(panel1);
		myjf.setVisible(true);



	}


}

