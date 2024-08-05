package sct;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import javax.swing.*;
import java.awt.Font;
//
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.awt.Graphics2D;

public class World extends JPanel{
	ArrayList<Bot> objects;
	Timer timer;
	int delay = 10;
	Random rand = new Random();
	//int[] world_scale = {324, 216};
	int[] world_scale = {162, 108};
	Bot[][] Map = new Bot[world_scale[0]][world_scale[1]];//0 - none, 1 - bot, 2 - organics
	Color gray = new Color(100, 100, 100);
	Color green = new Color(0, 255, 0);
	Color red = new Color(255, 0, 0);
	Color black = new Color(0, 0, 0);
	Color white = new Color(255, 255, 255);
	int steps = 0;
	int draw_type = 0;
	int b_count = 0;
	int obj_count = 0;
	int org_count = 0;
	String txt;
	String txt2;
	int mouse = 0;
	int W = 1920;
	int H = 1080;
	JButton stop_button = new JButton("Stop");
	boolean pause = false;
	boolean render = true;
	Bot selection = null;
	int[] botpos = new int [2];
	int[] for_set;
	JButton save_button = new JButton("Save");
	JButton show_brain_button = new JButton("Show brain");
	JButton render_button = new JButton("Render: on");
	JButton record_button = new JButton("Record: off");
	JTextField for_save = new JTextField();
	JTextField for_load = new JTextField();
	JButton predators_button;
	JButton energy_button;
	JButton color_button;
	JButton minerals_button;
	JButton relatives_button;
	JButton age_button;
	JButton load_bot_button;
	JButton load_world_button;
	JButton save_world_button;
	JButton new_population_button;
	JButton param_button;
	JButton kill_button;
	JButton select_button;
	JButton set_button;
	JButton remove_button;
	JButton other_button;
	JButton close_draw_types_button;
	JButton clans_button;
	boolean sh_brain = false;
	boolean rec = false;
	int menu = 0;
	int[] params = new int[1];
	public World() {
		setLayout(null);
		timer = new Timer(delay, new BotListener());
		objects = new ArrayList<Bot>();
		setBackground(gray);
		addMouseListener(new BotListener());
		addMouseMotionListener(new BotListener());
		//
		stop_button.addActionListener(new start_stop());
		stop_button.setBounds(W - 300, 125, 255, 35);
        add(stop_button);
        //
        predators_button = new JButton("Predators");
        predators_button.addActionListener(new change_draw_type(0));
		predators_button.setBounds(W - 300, 190, 125, 20);
        add(predators_button);
        //
        energy_button = new JButton("Energy");
        energy_button.addActionListener(new change_draw_type(2));
		energy_button.setBounds(W - 170, 190, 125, 20);
        add(energy_button);
        //
        minerals_button = new JButton("Minerals");
		minerals_button.setBounds(W - 300, 215, 125, 20);
		minerals_button.addActionListener(new change_draw_type(3));
        add(minerals_button);
        //
        age_button = new JButton("Age");
        age_button.addActionListener(new change_draw_type(4));
		age_button.setBounds(W - 170, 215, 125, 20);
        add(age_button);
        //
        color_button = new JButton("Color");
        color_button.addActionListener(new change_draw_type(1));
		color_button.setBounds(W - 300, 240, 125, 20);
        add(color_button);
        //
        relatives_button = new JButton("Relatives");
        relatives_button.addActionListener(new change_draw_type(7));
        relatives_button.setBounds(W - 300, 40, 125, 20);
		//
		clans_button = new JButton("Clans");
		clans_button.addActionListener(new change_draw_type(6));
		clans_button.setBounds(W - 170, 40, 125, 20);
        //
        select_button = new JButton("Select");
        select_button.addActionListener(new select());
		select_button.setBounds(W - 300, 475, 95, 20);
        add(select_button);
        //
        set_button = new JButton("Set");
        set_button.addActionListener(new set());
        set_button.setBounds(W - 200, 475, 95, 20);
        add(set_button);
        //
        remove_button = new JButton("Remove");
        remove_button.addActionListener(new remove());
        remove_button.setBounds(W - 100, 475, 95, 20);
        add(remove_button);
        //
        save_button.addActionListener(new save_bot());
        save_button.setBounds(W - 300, 385, 125, 20);
        save_button.setEnabled(false);
        add(save_button);
        //
        show_brain_button.addActionListener(new shbr());
        show_brain_button.setBounds(W - 170, 385, 125, 20);
        show_brain_button.setEnabled(false);
        add(show_brain_button);
        //
        for_save.setBounds(W - 300, 430, 250, 20);
        add(for_save);
        //
        for_load.setBounds(W - 300, 535, 250, 20);
        add(for_load);
        //
        load_bot_button = new JButton("Load bot");
        load_bot_button.addActionListener(new load_bot());
        load_bot_button.setBounds(W - 300, 560, 90, 20);
        add(load_bot_button);
        //
        load_world_button = new JButton("Load world");
        load_world_button.addActionListener(new load_world());
        load_world_button.setBounds(W - 205, 560, 90, 20);
        add(load_world_button);
        //
        save_world_button = new JButton("Save world");
        save_world_button.addActionListener(new save_world());
        save_world_button.setBounds(W - 110, 560, 90, 20);
        add(save_world_button);
        //
        new_population_button = new JButton("New population");
        new_population_button.addActionListener(new nwp());
        new_population_button.setBounds(W - 300, 610, 125, 20);
        add(new_population_button);
        //
        render_button.addActionListener(new rndr());
        render_button.setBounds(W - 300, 635, 125, 20);
        add(render_button);
        //
        record_button.addActionListener(new rcrd());
        record_button.setBounds(W - 170, 635, 125, 20);
        add(record_button);
        //
        kill_button = new JButton("Kill all");
        kill_button.addActionListener(new kill_all());
        kill_button.setBounds(W - 170, 610, 125, 20);
        add(kill_button);
        //
        other_button = new JButton("Other...");
        other_button.addActionListener(new open_draw_types());
		other_button.setBounds(W - 170, 240, 125, 20);
		add(other_button);
        //
        close_draw_types_button = new JButton("Close");
        close_draw_types_button.addActionListener(new close_draw_types());
        close_draw_types_button.setBounds(W - 300, 0, 255, 35);
        //
		timer.start();
	}
	public void remove_main() {
		remove(stop_button);
		remove(predators_button);
		remove(energy_button);
		remove(minerals_button);
		remove(age_button);
		remove(relatives_button);
		remove(color_button);
		remove(render_button);
		remove(record_button);
		remove(for_save);
		remove(for_load);
		remove(save_button);
		remove(show_brain_button);
		remove(new_population_button);
		remove(save_world_button);
		remove(load_world_button);
		remove(save_button);
		remove(load_bot_button);
		remove(kill_button);
		remove(set_button);
		remove(select_button);
		remove(remove_button);
		remove(other_button);
	}
	public void add_main() {
		add(stop_button);
		add(predators_button);
		add(energy_button);
		add(minerals_button);
		add(age_button);
		add(relatives_button);
		add(color_button);
		add(render_button);
		add(record_button);
		add(for_save);
		add(for_load);
		add(save_button);
		add(show_brain_button);
		add(new_population_button);
		add(save_world_button);
		add(load_world_button);
		add(save_button);
		add(load_bot_button);
		add(kill_button);
		add(set_button);
		add(select_button);
		add(remove_button);
		add(other_button);
	}
	public boolean find_map_pos(int[] pos, int state) {
		if (Map[pos[0]][pos[1]] != null) {
			if (Map[pos[0]][pos[1]].state == state) {
				return(true);
			}
		}
		return(false);
	}
	public void paintComponent(Graphics canvas) {
		super.paintComponent(canvas);
		canvas.setColor(white);
		canvas.fillRect(0, 0, W - 300, 1080);
		if (render) {
			for(Bot b: objects) {//рисуем ботов
				b.Draw(canvas, draw_type);
			}
			//for (int x = 0; x < world_scale[0]; x++) {//для отладки
			//	for (int y = 0; y < world_scale[1]; y++) {
			//		if (Map[x][y] != null) {
			//			if (Map[x][y].state == 0) {
			//				canvas.setColor(green);
			//				canvas.fillRect(x * 10, y * 10, 5, 5);
			//			}else if (Map[x][y].state == 1){
			//				canvas.setColor(red);
			//				canvas.fillRect(x * 10, y * 10, 5, 5);
			//			}
			//		}
			//	}
			//}
		}
		if (menu == 0) {//
			canvas.setColor(black);
			canvas.setFont(new Font("arial", Font.BOLD, 18));
			canvas.drawString("Main: ", W - 300, 20);
			canvas.drawString("version 2.0", W - 300, 40);
			canvas.drawString("steps: " + String.valueOf(steps), W - 300, 60);
			canvas.drawString("objects: " + String.valueOf(obj_count) + ", bots: " + String.valueOf(b_count), W - 300, 80);
			if (draw_type == 0) {
				txt = "predators view";
			}else if (draw_type == 1) {
				txt = "color view";
			}else if (draw_type == 2) {
				txt = "energy view";
			}else if (draw_type == 3) {
				txt = "minerals view";
			}else if (draw_type == 4){
				txt = "age view";
			}else if (draw_type == 5) {
				txt = "virus view";
			}else if (draw_type == 6) {
				txt = "clans view";
			}else if (draw_type == 7) {
				txt = "relatives view";
			}
			canvas.drawString("render type: " + txt, W - 300, 100);
			if (mouse == 0) {
				txt2 = "select";
			}else if (mouse == 1) {
				txt2 = "set";
			}else {
				txt2 = "remove";
			}
			canvas.drawString("mouse function: " + txt2, W - 300, 120);
			canvas.drawString("Render types:", W - 300, 180);
			canvas.drawString("Selection:", W - 300, 275);
			canvas.drawString("enter name:", W - 300, 425);
			canvas.drawString("Mouse functions:", W - 300, 470);
			canvas.drawString("Load:", W - 300, 510);
			canvas.drawString("enter name:", W - 300, 530);
			canvas.drawString("Controls:", W - 300, 600);
			if (selection != null) {
				if (selection.state == 0) {
					canvas.drawString("bot", W - 300, 295);
					canvas.drawString("energy: " + String.valueOf(selection.energy) + ", minerals: " + String.valueOf(selection.minerals), W - 300, 315);
					canvas.drawString("age: " + String.valueOf(selection.age), W - 300, 335);
					canvas.drawString("position: " + "[" + String.valueOf(selection.xpos) + ", " + String.valueOf(selection.ypos) + "]", W - 300, 355);
					canvas.drawString("color: " + "(" + String.valueOf(selection.color.getRed()) + ", " + String.valueOf(selection.color.getGreen()) + ", " + String.valueOf(selection.color.getBlue()) + ")", W - 300, 375);
				}else if (selection.state == 1) {
					canvas.drawString("organics", W - 300, 295);
					canvas.drawString("energy: " + String.valueOf(selection.energy), W - 300, 315);
					canvas.drawString("position: " + "[" + String.valueOf(selection.xpos) + ", " + String.valueOf(selection.ypos) + "]", W - 300, 335);
				}
				canvas.setColor(new Color(0, 0, 0, 200));
				canvas.fillRect(0, 0, W - 300, 1080);
				canvas.setColor(new Color(255, 0, 0));
				if (selection.state == 0) {
					canvas.fillRect(selection.xpos * 10, selection.ypos * 10, 10, 10);
				}else if (selection.state == 1) {
					canvas.fillRect(1 + selection.xpos * 10, 1 + selection.ypos * 10, 8, 8);
				}
				//if (selection.enr_chain_next != null) {//для отладки
				//	canvas.setColor(new Color(255, 0, 255));
				//	canvas.fillRect(selection.enr_chain_next.xpos * 10, selection.enr_chain_next.ypos * 10, 10, 10);
				//}
				//if (selection.enr_chain_prev != null) {
				//	canvas.setColor(new Color(255, 255, 0));
				//	canvas.fillRect(selection.enr_chain_prev.xpos * 10, selection.enr_chain_prev.ypos * 10, 10, 10);
				//}
			}else {
				canvas.drawString("none", W - 300, 295);
			}
			if (sh_brain) {
				canvas.setColor(new Color(90, 90, 90));
				canvas.fillRect(0, 0, 360, 360);
				canvas.setColor(new Color(128, 128, 128));
				for (int x = 0; x < 8; x++) {
					for (int y = 0; y < 8; y++) {
						canvas.setColor(new Color(128, 128, 128));
						canvas.fillRect(x * 45, y * 45, 40, 40);
						canvas.setColor(new Color(0, 0, 0));
						canvas.drawString(String.valueOf(selection.commands[x + y * 8]), x * 45 + 20, y * 45 + 20);
					}
				}
			}
		}else {
			canvas.setColor(gray);
			canvas.drawRect(0, 0, W, H);
		}
		//запись
		if (rec && steps % 25 == 0) {
			try {
				BufferedImage buff = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = buff.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 0);
				}
				g2d.dispose();
				//
				BufferedImage buff2 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				g2d = buff2.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 2);
				}
				g2d.dispose();
				//
				BufferedImage buff3 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				g2d = buff3.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 1);
				}
				g2d.dispose();
				//
				BufferedImage buff4 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				g2d = buff4.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 6);
				}
				g2d.dispose();
				//
				ImageIO.write(buff, "png", new File("record/predators/screen" + String.valueOf(steps / 25)+ ".png"));
				ImageIO.write(buff2, "png", new File("record/energy/screen" + String.valueOf(steps / 25)+ ".png"));
				ImageIO.write(buff3, "png", new File("record/color/screen" + String.valueOf(steps / 25)+ ".png"));
				ImageIO.write(buff4, "png", new File("record/clans/screen" + String.valueOf(steps / 25)+ ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void newPopulation() {
		steps = 0;
		objects = new ArrayList<Bot>();
		Map = new Bot[world_scale[0]][world_scale[1]];//0 - none, 1 - bot, 2 - organics
		for (int i = 0; i < 1000; i++) {
			while(true){
				int x = rand.nextInt(world_scale[0]);
				int y = rand.nextInt(world_scale[1]);
				if (Map[x][y] == null) {
					Bot new_bot;
					new_bot = new Bot(
						x,
						y,
						new Color(rand.nextInt(256),rand.nextInt(256), rand.nextInt(256)),
						1000,
						Map,
						objects
					);
					new_bot.self = new_bot;
					objects.add(new_bot);
					Map[x][y] = new_bot;
					break;
				}
			}
		}
		repaint();
	}
	private class BotListener extends MouseAdapter implements ActionListener{
		public void mousePressed(MouseEvent e) {
			if (e.getX() < W - 300) {
				botpos[0] = e.getX() / 10;
				botpos[1] = e.getY() / 10;
				if (mouse == 0) {//select
					if (Map[botpos[0]][botpos[1]] != null) {
						Bot b = Map[botpos[0]][botpos[1]];
						selection = b;
						if (b.state == 0) {
							save_button.setEnabled(true);
							show_brain_button.setEnabled(true);
						}else {
							save_button.setEnabled(false);
							show_brain_button.setEnabled(false);
							sh_brain = false;
						}
					}else {
						selection = null;
						save_button.setEnabled(false);
						show_brain_button.setEnabled(false);
						sh_brain = false;
					}
				}else if (mouse == 1) {//set
					if (for_set != null) {
						if (Map[botpos[0]][botpos[1]] == null) {
							if (for_set != null) {
								Bot new_bot;
								new_bot = new Bot(botpos[0], botpos[1], new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)), 1000, Map, objects);
								new_bot.self = new_bot;
								for (int i = 0; i < 64 + 3; i++) {
									new_bot.commands[i] = for_set[i];
								}
								objects.add(new_bot);
								Map[botpos[0]][botpos[1]] = new_bot;
							}
						}
					}
				}else {//remove
					if (Map[botpos[0]][botpos[1]] != null) {
						Bot b = Map[botpos[0]][botpos[1]];
						b.energy = 0;
						b.killed = 1;
						Map[botpos[0]][botpos[1]] = null;
					}
				}
			}
		}
		public void mouseDragged(MouseEvent e) {
			if (e.getX() < W - 300) {
				botpos[0] = e.getX() / 10;
				botpos[1] = e.getY() / 10;
				if (mouse == 1) {//set
					if (Map[botpos[0]][botpos[1]] == null) {
						if (for_set != null) {
							Bot new_bot;
							new_bot = new Bot(botpos[0], botpos[1], new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)), 1000, Map, objects);
							new_bot.self = new_bot;
							for (int i = 0; i < 64 + 3; i++) {
								new_bot.commands[i] = for_set[i];
							}
							objects.add(new_bot);
							Map[botpos[0]][botpos[1]] = new_bot;
						}
					}
				}else if (mouse == 2) {//remove
					if (Map[botpos[0]][botpos[1]] != null) {
						Bot b = Map[botpos[0]][botpos[1]];
						b.energy = 0;
						b.killed = 1;
						Map[botpos[0]][botpos[1]] = null;
					}
				}
			}
		}
		public void actionPerformed(ActionEvent e) {
			if (!pause) {
				steps++;
				b_count = 0;
				obj_count = 0;
				org_count = 0;
				ListIterator<Bot> bot_iterator = objects.listIterator();
				while (bot_iterator.hasNext()) {
					Bot next_bot = bot_iterator.next();
					next_bot.Update(bot_iterator, steps);
					if (selection != null) {
						if (next_bot.xpos == selection.xpos && next_bot.ypos == selection.ypos) {
							if (next_bot != selection) {
								selection = null;
								save_button.setEnabled(false);
								show_brain_button.setEnabled(false);
								sh_brain = false;
							}
						}
					}
					obj_count++;
					if (next_bot.state != 0) {
						org_count++;
					}else {
						b_count++;
					}
				}
				if (selection != null) {
					int[] pos = {selection.xpos, selection.ypos};
					if (selection.killed == 1 || Map[pos[0]][pos[1]] == null){
						selection = null;
						save_button.setEnabled(false);
						show_brain_button.setEnabled(false);
						sh_brain = false;
					}
				}
			}
			ListIterator<Bot> iterator = objects.listIterator();
			while (iterator.hasNext()) {
				Bot next_bot = iterator.next();
				if (next_bot.killed == 1) {
					iterator.remove();
				}
			}
			repaint();
			
		}
		
	}
	private class change_draw_type implements ActionListener{
		int number;
		private change_draw_type(int new_number){
			number = new_number;
		}
		public void actionPerformed(ActionEvent e) {
			draw_type = number;
		}
	}
	private class start_stop implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			pause = !pause;
			if (pause) {
				stop_button.setText("Start");
			}else {
				stop_button.setText("Stop");
			}
		}
	}
	private class select implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			mouse = 0;
		}
	}
	private class set implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			mouse = 1;
		}
	}
	private class remove implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			mouse = 2;
		}
	}
	private class nwp implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			newPopulation();
		}
	}
	private class rndr implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			render = !render;
			if (render) {
				render_button.setText("Render: on");
			}else {
				render_button.setText("Render: off");
			}
		}
	}
	private class rcrd implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			rec = !rec;
			if (rec) {
				record_button.setText("Record: on");
			}else {
				record_button.setText("Record: off");
			}
		}
	}
	private class shbr implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			sh_brain = !sh_brain;
			if (pause == false) {
				pause = true;
			}else if (sh_brain == false) {
				pause = false;
			}
		}
	}
	private class kill_all implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			steps = 0;
			objects = new ArrayList<Bot>();
			Map = new Bot[162][108];//0 - none, 1 - bot, 2 - organics
		}
	}
	private class save_bot implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			String txt = "";
			for (int i = 0; i < 64 + 3; i++) {
				txt += String.valueOf(selection.commands[i]) + " ";
			}
			try {
	            FileWriter fileWriter = new FileWriter("saved objects/" + for_save.getText() + ".dat");
	            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	 
	            bufferedWriter.write(txt);
	 
	            bufferedWriter.close();
	        } catch (IOException ex) {
	            System.out.println("Ошибка при записи в файл");
	            ex.printStackTrace();
	        }
		}
	}
	private class load_bot implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try {
	            FileReader fileReader = new FileReader("saved objects/" + for_load.getText() + ".dat");
	            BufferedReader bufferedReader = new BufferedReader(fileReader);
	 
	            String line = bufferedReader.readLine();
	 
	            bufferedReader.close();
	            
	            String[] l = line.split(" ");
	            for_set = new int[64 + 3];
	            for (int i = 0; i < 64 + 3; i++) {
	            	for_set[i] = Integer.parseInt(l[i]);
	            }
	        } catch (IOException ex) {
	            System.out.println("Ошибка при чтении файла");
	            ex.printStackTrace();
	        }
		}
	}
	private class load_world implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try {
	            FileReader fileReader = new FileReader("saved worlds/" + for_load.getText() + ".dat");
	            BufferedReader bufferedReader = new BufferedReader(fileReader);
	 
	            String line = bufferedReader.readLine();
	 
	            bufferedReader.close();
	            
	            String[] l = line.split(";");
	            steps = Integer.parseInt(l[0]);
	            objects = new ArrayList<Bot>();
	    		Map = new Bot[world_scale[0]][world_scale[1]];//0 - none, 1 - bot, 2 - organics
	    		
	    		for (int i = 1; i < l.length; i++) {
	    			String[] bot_data = l[i].split(":");
	    			Bot new_bot;
	    			new_bot = new Bot(
	    				Integer.parseInt(bot_data[3]),
	    				Integer.parseInt(bot_data[4]),
	    				new Color(Integer.parseInt(bot_data[10]), Integer.parseInt(bot_data[11]), Integer.parseInt(bot_data[12])),
	    				Integer.parseInt(bot_data[0]),
	    				Map,
	    				objects
	    			);
	    			new_bot.self = new_bot;
	    			new_bot.age = Integer.parseInt(bot_data[1]);
	    			new_bot.minerals = Integer.parseInt(bot_data[2]);
	    			new_bot.rotate = Integer.parseInt(bot_data[5]);
	    			new_bot.state = Integer.parseInt(bot_data[6]);
	    			new_bot.c_red = Integer.parseInt(bot_data[7]);
	    			new_bot.c_green = Integer.parseInt(bot_data[8]);
	    			new_bot.c_blue = Integer.parseInt(bot_data[9]);
	    			new_bot.index = Integer.parseInt(bot_data[13]);
	    			new_bot.killed = Integer.parseInt(bot_data[14]);
	    			for (int j = 0; j < 64; j++) {
	    				new_bot.commands[j] = Integer.parseInt(bot_data[15 + j]);;
	    			}
	    			Map[Integer.parseInt(bot_data[3])][Integer.parseInt(bot_data[4])] = new_bot;
	    			objects.add(new_bot);
	    		}
	        } catch (IOException ex) {
	            System.out.println("Ошибка при чтении файла");
	            ex.printStackTrace();
	        }
		}
	}
	private class save_world implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			String txt = "";
			txt += String.valueOf(steps) + ";";
			for(Bot b: objects) {//bot length - 79
				txt += String.valueOf(b.energy) + ":";//0
				txt += String.valueOf(b.age) + ":";//1
				txt += String.valueOf(b.minerals) + ":";//2
				txt += String.valueOf(b.xpos) + ":";//3
				txt += String.valueOf(b.ypos) + ":";//4
				txt += String.valueOf(b.rotate) + ":";//5
				txt += String.valueOf(b.state) + ":";//6
				txt += String.valueOf(b.c_red) + ":";//7
				txt += String.valueOf(b.c_green) + ":";//8
				txt += String.valueOf(b.c_blue) + ":";//9
				txt += String.valueOf(b.color.getRed()) + ":";//10
				txt += String.valueOf(b.color.getGreen()) + ":";//11
				txt += String.valueOf(b.color.getBlue()) + ":";//12
				txt += String.valueOf(b.index) + ":";//13
				txt += String.valueOf(b.killed) + ":";//14
				for (int i = 0; i < 64; i++) {//15 - 78
					txt += String.valueOf(b.commands[i]) + ":";
				}
				txt += ";";
			}
			try {
	            FileWriter fileWriter = new FileWriter("saved worlds/" + for_load.getText() + ".dat");
	            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	 
	            bufferedWriter.write(txt);
	 
	            bufferedWriter.close();
	        } catch (IOException ex) {
	            System.out.println("Ошибка при записи в файл");
	            ex.printStackTrace();
	        }
		}
	}
	private class open_draw_types implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			remove_main();
			add(relatives_button);
			add(clans_button);
			add(close_draw_types_button);
			menu = 1;
		}
	}
	private class close_draw_types implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			add_main();
			remove(relatives_button);
			remove(clans_button);
			remove(close_draw_types_button);
			menu = 0;
		}
	}
}
