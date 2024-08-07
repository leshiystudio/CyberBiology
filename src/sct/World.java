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
	ArrayList<Bot> objects;//массив объектов
	Timer timer;
	int delay = 1;//минимальная задержка между кадрами
	Random rand = new Random();//генератор случайных чисел
	//int[] world_scale = {324, 216};
	int[] world_scale = {162, 108};//размер мира
	Bot[][] Map = new Bot[world_scale[0]][world_scale[1]];//карта со ссылками на объекты, находящиеся в ней
	int W = 1920;//размер экрана
	int H = 1080;
	//цвета
	Color gray = new Color(100, 100, 100);
	Color black = new Color(0, 0, 0);
	Color white = new Color(255, 255, 255);
	//
	int steps = 0;//счетчик шагов
	int b_count = 0;//счетчик ботов
	int obj_count = 0;//счетчик объектов
	int org_count = 0;//счетчик органики
	//
	String txt;//для отображения названия режима отрисовки
	String txt2;
	//
	int mouse = 0;//функция мыши
	int menu = 0;//что отображать в панели управления
	int draw_type = 0;//режим отрисовки
	//
	boolean pause = false;//включена ли симуляция?
	boolean render = true;//включена ли отрисовка?
	boolean sh_brain = false;//включено ли отображение мозга?
	boolean rec = false;//включена ли запись?
	//
	Bot selection = null;//выбранное существо
	int[] botpos = new int [2];//позиция выбранного существа
	int[] for_set;//мозг для загрузки из файла
	//кнопки
	JButton stop_button = new JButton("Stop");
	JButton save_button = new JButton("Save");
	JButton show_brain_button = new JButton("Show brain");
	JButton render_button = new JButton("Render: on");
	JButton record_button = new JButton("Record: off");
	JTextField for_save = new JTextField();//для ввода имени файла
	JTextField for_load = new JTextField();//для ввода имени файла
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
	public World() {
		setLayout(null);
		timer = new Timer(delay, new BotListener());
		objects = new ArrayList<Bot>();
		setBackground(gray);
		addMouseListener(new BotListener());
		addMouseMotionListener(new BotListener());
		//кнопка паузы
		stop_button.addActionListener(new start_stop());
		stop_button.setBounds(W - 300, 125, 255, 35);
        add(stop_button);
        //кнопка режима отрисовки хищников
        predators_button = new JButton("Predators");
        predators_button.addActionListener(new change_draw_type(0));
		predators_button.setBounds(W - 300, 190, 125, 20);
        add(predators_button);
        //кнопка режима отрисовки энергии
        energy_button = new JButton("Energy");
        energy_button.addActionListener(new change_draw_type(2));
		energy_button.setBounds(W - 170, 190, 125, 20);
        add(energy_button);
        //кнопка режима отрисовки минералов
        minerals_button = new JButton("Minerals");
		minerals_button.setBounds(W - 300, 215, 125, 20);
		minerals_button.addActionListener(new change_draw_type(3));
        add(minerals_button);
        //кнопка режима отрисовки возраста
        age_button = new JButton("Age");
        age_button.addActionListener(new change_draw_type(4));
		age_button.setBounds(W - 170, 215, 125, 20);
        add(age_button);
        //кнопка режима отрисовки цвета
        color_button = new JButton("Color");
        color_button.addActionListener(new change_draw_type(1));
		color_button.setBounds(W - 300, 240, 125, 20);
        add(color_button);
        //кнопка режима отрисовки родственников(не работает)
        relatives_button = new JButton("Relatives");
        relatives_button.addActionListener(new change_draw_type(7));
        relatives_button.setBounds(W - 300, 40, 125, 20);
		//кнопка режима отрисовки кланов
		clans_button = new JButton("Clans");
		clans_button.addActionListener(new change_draw_type(6));
		clans_button.setBounds(W - 170, 40, 125, 20);
        //кнопка выбора бота
        select_button = new JButton("Select");
        select_button.addActionListener(new select());
		select_button.setBounds(W - 300, 475, 95, 20);
        add(select_button);
        //кнопка установки бота
        set_button = new JButton("Set");
        set_button.addActionListener(new set());
        set_button.setBounds(W - 200, 475, 95, 20);
        add(set_button);
        //кнопка удаления бота
        remove_button = new JButton("Remove");
        remove_button.addActionListener(new remove());
        remove_button.setBounds(W - 100, 475, 95, 20);
        add(remove_button);
        //кнопка сохранения бота
        save_button.addActionListener(new save_bot());
        save_button.setBounds(W - 300, 385, 125, 20);
        save_button.setEnabled(false);
        add(save_button);
        //кнопка просмотра мозга
        show_brain_button.addActionListener(new shbr());
        show_brain_button.setBounds(W - 170, 385, 125, 20);
        show_brain_button.setEnabled(false);
        add(show_brain_button);
        //поле для ввода имени файла
        for_save.setBounds(W - 300, 430, 250, 20);
        add(for_save);
        for_load.setBounds(W - 300, 535, 250, 20);
        add(for_load);
        //кнопка загрузки бота
        load_bot_button = new JButton("Load bot");
        load_bot_button.addActionListener(new load_bot());
        load_bot_button.setBounds(W - 300, 560, 90, 20);
        add(load_bot_button);
        //кнопка загрузки мира(не работает)
        load_world_button = new JButton("Load world");
        load_world_button.addActionListener(new load_world());
        load_world_button.setBounds(W - 205, 560, 90, 20);
        add(load_world_button);
        //кнопка сохранения мира
        save_world_button = new JButton("Save world");
        save_world_button.addActionListener(new save_world());
        save_world_button.setBounds(W - 110, 560, 90, 20);
        add(save_world_button);
        //кнопка создания случаюной популяции
        new_population_button = new JButton("New population");
        new_population_button.addActionListener(new nwp());
        new_population_button.setBounds(W - 300, 610, 125, 20);
        add(new_population_button);
        //кнопка выключения отрисовки
        render_button.addActionListener(new rndr());
        render_button.setBounds(W - 300, 635, 125, 20);
        add(render_button);
        //кнопка включения записи
        record_button.addActionListener(new rcrd());
        record_button.setBounds(W - 170, 635, 125, 20);
        add(record_button);
        //кнопка удаления всех ботов
        kill_button = new JButton("Kill all");
        kill_button.addActionListener(new kill_all());
        kill_button.setBounds(W - 170, 610, 125, 20);
        add(kill_button);
        //кнопка открытия выбора дополнительных режимов отрисовки
        other_button = new JButton("Other...");
        other_button.addActionListener(new open_draw_types());
		other_button.setBounds(W - 170, 240, 125, 20);
		add(other_button);
        //кнопка закрытия выбора дополнительных режимов отрисовки
        close_draw_types_button = new JButton("Close");
        close_draw_types_button.addActionListener(new close_draw_types());
        close_draw_types_button.setBounds(W - 300, 0, 255, 35);
        //
		timer.start();
	}
	public void remove_main() {//удалить кнопки основного интерфейса
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
	public void add_main() {//добавить кнопки основного интерфейса
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
	public boolean find_map_pos(int[] pos, int state) {//есть ли нужный объект на карте
		if (Map[pos[0]][pos[1]] != null) {
			if (Map[pos[0]][pos[1]].state == state) {
				return(true);
			}
		}
		return(false);
	}
	public void paintComponent(Graphics canvas) {//рисование
		super.paintComponent(canvas);
		canvas.setColor(white);//залить мир белым
		canvas.fillRect(0, 0, W - 300, 1080);
		if (render) {//все, для чего нужна включенная отрисовка
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
		if (menu == 0) {//рисовать основной интерфейс
			canvas.setColor(black);//цвет шрифта
			canvas.setFont(new Font("arial", Font.BOLD, 18));//шрифт
			//рисуем текст
			canvas.drawString("Main: ", W - 300, 20);
			canvas.drawString("version 2.0", W - 300, 40);
			canvas.drawString("steps: " + String.valueOf(steps), W - 300, 60);
			canvas.drawString("objects: " + String.valueOf(obj_count) + ", bots: " + String.valueOf(b_count), W - 300, 80);
			if (draw_type == 0) {//режим отрисовки
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
			if (selection != null) {//информация о выбранном боте
				if (selection.state == 0) {//если бот
					canvas.drawString("bot", W - 300, 295);
					canvas.drawString("energy: " + String.valueOf(selection.energy) + ", minerals: " + String.valueOf(selection.minerals), W - 300, 315);//сколько энергии и минералов
					canvas.drawString("age: " + String.valueOf(selection.age), W - 300, 335);//сколько осталось жить
					canvas.drawString("position: " + "[" + String.valueOf(selection.xpos) + ", " + String.valueOf(selection.ypos) + "]", W - 300, 355);//позиция
					canvas.drawString("color: " + "(" + String.valueOf(selection.color.getRed()) + ", " + String.valueOf(selection.color.getGreen()) + ", " + String.valueOf(selection.color.getBlue()) + ")", W - 300, 375);//цвет
				}else if (selection.state == 1) {//если органика
					canvas.drawString("organics", W - 300, 295);
					canvas.drawString("energy: " + String.valueOf(selection.energy), W - 300, 315);//сколько энергии запасено
					canvas.drawString("position: " + "[" + String.valueOf(selection.xpos) + ", " + String.valueOf(selection.ypos) + "]", W - 300, 335);//позиция
				}
				//полупрозрачный квадрат, чтобы выделение было лучше видно
				canvas.setColor(new Color(0, 0, 0, 200));
				canvas.fillRect(0, 0, W - 300, 1080);
				//красное выделение
				canvas.setColor(new Color(255, 0, 0));
				if (selection.state == 0) {//у бота выделение больше, чем у органики
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
			}else {//если никто не выбран, пишем "none"
				canvas.drawString("none", W - 300, 295);
			}
			if (sh_brain) {//рисуем мозг бота
				canvas.setColor(new Color(90, 90, 90));
				canvas.fillRect(0, 0, 360, 360);//серый квадрат
				for (int x = 0; x < 8; x++) {//проходим по всем строкам и столбцам
					for (int y = 0; y < 8; y++) {
						canvas.setColor(new Color(128, 128, 128));//рисуем светло - серый квадрат
						canvas.fillRect(x * 45, y * 45, 40, 40);
						canvas.setColor(new Color(0, 0, 0));
						canvas.drawString(String.valueOf(selection.commands[x + y * 8]), x * 45 + 20, y * 45 + 20);//рисуем нужную команду
					}
				}
			}
		}else {//рисовать интерфейс выбора дополнительного режима отрисовки
			canvas.setColor(gray);
			canvas.drawRect(0, 0, W, H);
		}
		if (rec && steps % 25 == 0) {//запись
			try {//чтобы не вылетело
				//для записи используется костыль с сохранением bufferedimage в файл и рисование при помощи graphics2d
				//режим отрисовки хищников
				BufferedImage buff = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = buff.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 0);
				}
				g2d.dispose();
				//режим отрисовки энергии
				BufferedImage buff2 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				g2d = buff2.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 2);
				}
				g2d.dispose();
				//режим отрисовки цвета
				BufferedImage buff3 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				g2d = buff3.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 1);
				}
				g2d.dispose();
				//режим отрисовки кланов
				BufferedImage buff4 = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
				g2d = buff4.createGraphics();
				g2d.setColor(Color.WHITE);
				g2d.fillRect(0, 0, 1920, 1080);
				for(Bot b: objects) {
					b.Draw(g2d, 6);
				}
				g2d.dispose();
				//сохранение в файл
				ImageIO.write(buff, "png", new File("record/predators/screen" + String.valueOf(steps / 25)+ ".png"));
				ImageIO.write(buff2, "png", new File("record/energy/screen" + String.valueOf(steps / 25)+ ".png"));
				ImageIO.write(buff3, "png", new File("record/color/screen" + String.valueOf(steps / 25)+ ".png"));
				ImageIO.write(buff4, "png", new File("record/clans/screen" + String.valueOf(steps / 25)+ ".png"));
			} catch (IOException e) {//если нет папок, то сделать ошибку
				e.printStackTrace();
			}
		}
	}
	public void newPopulation() {//создать случайную популяцию
		steps = 0;//сбросить счетчик шагов
		objects = new ArrayList<Bot>();//сбросить массив с объектами
		Map = new Bot[world_scale[0]][world_scale[1]];//сбросиь карту
		for (int i = 0; i < 1000; i++) {//создать 1000 ботов
			while(true){//чтобы 2 бота не появились на 1 клетке
				int x = rand.nextInt(world_scale[0]);//случайная позиция
				int y = rand.nextInt(world_scale[1]);
				if (Map[x][y] == null) {//если на клетке с ботом никого нет, создаем случайного бота, запускаем его в мир и завершаем while(true)
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
		repaint();//фиг его знает что такое
	}
	private class BotListener extends MouseAdapter implements ActionListener{//клик по экрану
		public void mousePressed(MouseEvent e) {
			if (e.getX() < W - 300) {//если нажали не на панель управления
				botpos[0] = e.getX() / 10;//позиция клика(в клетках)
				botpos[1] = e.getY() / 10;
				if (mouse == 0) {//если режим выбора
					if (Map[botpos[0]][botpos[1]] != null) {//если на карте есть бот
						Bot b = Map[botpos[0]][botpos[1]];//выбрать бота
						selection = b;
						if (b.state == 0) {//если бот, включить кнопки сохранения и просмотра мозга, если органика - выключить
							save_button.setEnabled(true);
							show_brain_button.setEnabled(true);
						}else {
							save_button.setEnabled(false);
							show_brain_button.setEnabled(false);
							sh_brain = false;
						}
					}else {//если бота нет, сбрасываем предыдущее сохраненное существо
						selection = null;
						save_button.setEnabled(false);
						show_brain_button.setEnabled(false);
						sh_brain = false;
					}
				}else if (mouse == 1) {//если режим установки
					if (for_set != null) {//если есть мозг для установки
						if (Map[botpos[0]][botpos[1]] == null) {//если место клика пустое
							Bot new_bot;//создать нового бота, задать ему мозг и запустить в мир
							new_bot = new Bot(botpos[0], botpos[1], new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)), 1000, Map, objects);
							new_bot.self = new_bot;
							for (int i = 0; i < 64 + 3; i++) {//чтобы у разных установленных ботов мозги не ссылались на 1 объект
								new_bot.commands[i] = for_set[i];
							}
							objects.add(new_bot);
							Map[botpos[0]][botpos[1]] = new_bot;
						}
					}
				}else {//если режим удаления
					if (Map[botpos[0]][botpos[1]] != null) {//если на карте есть объект, удалить его
						Bot b = Map[botpos[0]][botpos[1]];
						b.energy = 0;
						b.killed = 1;
						Map[botpos[0]][botpos[1]] = null;
					}
				}
			}
		}
		public void mouseDragged(MouseEvent e) {//если мышка нажата и двигается
			if (e.getX() < W - 300) {//если нажали не на панель управления
				botpos[0] = e.getX() / 10;//позиция клика(в клетках)
				botpos[1] = e.getY() / 10;
				if (mouse == 1) {//если режим установки(выбирать так нельзя)
					if (Map[botpos[0]][botpos[1]] == null) {//если место клика пустое
						if (for_set != null) {//если есть мозг для установки
							Bot new_bot;//создать нового бота, задать ему мозг и запустить в мир
							new_bot = new Bot(botpos[0], botpos[1], new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)), 1000, Map, objects);
							new_bot.self = new_bot;
							for (int i = 0; i < 64 + 3; i++) {//чтобы у разных установленных ботов мозги не ссылались на 1 объект
								new_bot.commands[i] = for_set[i];
							}
							objects.add(new_bot);
							Map[botpos[0]][botpos[1]] = new_bot;
						}
					}
				}else if (mouse == 2) {//если режим удаления
					if (Map[botpos[0]][botpos[1]] != null) {//если на карте есть объект, удалить его
						Bot b = Map[botpos[0]][botpos[1]];
						b.energy = 0;
						b.killed = 1;
						Map[botpos[0]][botpos[1]] = null;
					}
				}
			}
		}
		public void actionPerformed(ActionEvent e) {//обновление всего
			if (!pause) {//если симуляция не остановлена
				steps++;//увеличить число шагов
				b_count = 0;//сбросить счетчики количества существ
				obj_count = 0;
				org_count = 0;
				//
				ListIterator<Bot> bot_iterator = objects.listIterator();
				while (bot_iterator.hasNext()) {//в цикле проходим по всем ботам
					Bot next_bot = bot_iterator.next();//бот
					next_bot.Update(bot_iterator, steps);//обновление бота
					if (selection != null) {//если выбрано существо
						if (next_bot.xpos == selection.xpos && next_bot.ypos == selection.ypos) {//если выбранное существо находится на месте обновляемого бота
							if (next_bot != selection) {//если на месте выбранного существа находится кто - то другой, сбросить выбранное существо
								selection = null;
								save_button.setEnabled(false);
								show_brain_button.setEnabled(false);
								sh_brain = false;
							}
						}
					}
					//увеличить счетчики
					obj_count++;
					if (next_bot.state != 0) {
						org_count++;
					}else {
						b_count++;
					}
				}
				if (selection != null) {//если выбранный бот убит, или в карте на его позиции никого нет, сбросить выбранного бота
					int[] pos = {selection.xpos, selection.ypos};
					if (selection.killed == 1 || Map[pos[0]][pos[1]] == null){
						selection = null;
						save_button.setEnabled(false);
						show_brain_button.setEnabled(false);
						sh_brain = false;
					}
				}
			}
			ListIterator<Bot> iterator = objects.listIterator();//удаление мертвых ботов
			while (iterator.hasNext()) {
				Bot next_bot = iterator.next();
				if (next_bot.killed == 1) {
					iterator.remove();
				}
			}
			repaint();//
			
		}
		
	}
	//функции для кнопок
	private class change_draw_type implements ActionListener{//смена режима отрисовки(берется из параметра)
		int number;
		private change_draw_type(int new_number){
			number = new_number;
		}
		public void actionPerformed(ActionEvent e) {
			draw_type = number;
		}
	}
	private class start_stop implements ActionListener{//пауза
		public void actionPerformed(ActionEvent e) {
			pause = !pause;
			if (pause) {
				stop_button.setText("Start");
			}else {
				stop_button.setText("Stop");
			}
		}
	}
	private class select implements ActionListener{//режим выбора
		public void actionPerformed(ActionEvent e) {
			mouse = 0;
		}
	}
	private class set implements ActionListener{//режим установки
		public void actionPerformed(ActionEvent e) {
			mouse = 1;
		}
	}
	private class remove implements ActionListener{//режим удаления
		public void actionPerformed(ActionEvent e) {
			mouse = 2;
		}
	}
	private class nwp implements ActionListener{//создание случайной популяции
		public void actionPerformed(ActionEvent e) {
			newPopulation();
		}
	}
	private class rndr implements ActionListener{//вкл/выкл отрисовки
		public void actionPerformed(ActionEvent e) {
			render = !render;
			if (render) {
				render_button.setText("Render: on");
			}else {
				render_button.setText("Render: off");
			}
		}
	}
	private class rcrd implements ActionListener{//вкл/выкл записи
		public void actionPerformed(ActionEvent e) {
			rec = !rec;
			if (rec) {
				record_button.setText("Record: on");
			}else {
				record_button.setText("Record: off");
			}
		}
	}
	private class shbr implements ActionListener{//просмотр мозга
		public void actionPerformed(ActionEvent e) {
			sh_brain = !sh_brain;
			if (pause == false) {
				pause = true;
			}else if (sh_brain == false) {
				pause = false;
			}
		}
	}
	private class kill_all implements ActionListener{//убить всех ботов
		public void actionPerformed(ActionEvent e) {
			steps = 0;//все сбрасывается
			objects = new ArrayList<Bot>();
			Map = new Bot[162][108];
		}
	}
	private class save_bot implements ActionListener{//сохранить бота
		public void actionPerformed(ActionEvent e) {
			String txt = "";
			for (int i = 0; i < 64 + 3; i++) {//превращение мозга в строку
				txt += String.valueOf(selection.commands[i]) + " ";
			}
			try {//сохранение в файл(имя файла из поля для ввода for_save)
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
	private class load_bot implements ActionListener{//загрузить бота
		public void actionPerformed(ActionEvent e) {
			try {
				//чтение файла(имя файла из поля для ввода for_load)
	            FileReader fileReader = new FileReader("saved objects/" + for_load.getText() + ".dat");
	            BufferedReader bufferedReader = new BufferedReader(fileReader);
	 
	            String line = bufferedReader.readLine();
	 
	            bufferedReader.close();
	            
	            String[] l = line.split(" ");//делим строку на куски
	            for_set = new int[64 + 3];//записываем в for_set
	            for (int i = 0; i < 64 + 3; i++) {
	            	for_set[i] = Integer.parseInt(l[i]);
	            }
	        } catch (IOException ex) {//ошибка
	            System.out.println("Ошибка при чтении файла");
	            ex.printStackTrace();
	        }
		}
	}
	private class load_world implements ActionListener{//загрузить мир(не работает)
		public void actionPerformed(ActionEvent e) {
			try {
				//чтение файла(имя файла из поля для ввода for_load)
	            FileReader fileReader = new FileReader("saved worlds/" + for_load.getText() + ".dat");
	            BufferedReader bufferedReader = new BufferedReader(fileReader);
	            String line = bufferedReader.readLine();
	            bufferedReader.close();
	            //сбрасываем текущий мир и делим строку на куски
	            String[] l = line.split(";");
	            steps = Integer.parseInt(l[0]);
	            objects = new ArrayList<Bot>();
	    		Map = new Bot[world_scale[0]][world_scale[1]];
	    		
	    		for (int i = 1; i < l.length; i++) {//боты
	    			String[] bot_data = l[i].split(":");//данные о боте
	    			Bot new_bot;
	    			new_bot = new Bot(//создем бота
	    				Integer.parseInt(bot_data[3]),
	    				Integer.parseInt(bot_data[4]),
	    				new Color(Integer.parseInt(bot_data[10]), Integer.parseInt(bot_data[11]), Integer.parseInt(bot_data[12])),
	    				Integer.parseInt(bot_data[0]),
	    				Map,
	    				objects
	    			);
	    			new_bot.self = new_bot;
	    			new_bot.age = Integer.parseInt(bot_data[1]);//возраст
	    			new_bot.minerals = Integer.parseInt(bot_data[2]);//минералы
	    			new_bot.rotate = Integer.parseInt(bot_data[5]);//направление
	    			new_bot.state = Integer.parseInt(bot_data[6]);//состояние(бот или органика)
	    			new_bot.c_red = Integer.parseInt(bot_data[7]);//красный(режим хищников)
	    			new_bot.c_green = Integer.parseInt(bot_data[8]);//зеленый(режим хищников)
	    			new_bot.c_blue = Integer.parseInt(bot_data[9]);//синий(режим хищников)
	    			new_bot.index = Integer.parseInt(bot_data[13]);//индекс
	    			new_bot.killed = Integer.parseInt(bot_data[14]);//убит ли бот
	    			for (int j = 0; j < 64; j++) {//мозг
	    				new_bot.commands[j] = Integer.parseInt(bot_data[15 + j]);;
	    			}
	    			Map[Integer.parseInt(bot_data[3])][Integer.parseInt(bot_data[4])] = new_bot;
	    			objects.add(new_bot);//запускаем в мир
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
			for(Bot b: objects) {//запись бота
				//длина бота - 79
				txt += String.valueOf(b.energy) + ":";//0 энергия
				txt += String.valueOf(b.age) + ":";//1 возраст
				txt += String.valueOf(b.minerals) + ":";//2 минералы
				txt += String.valueOf(b.xpos) + ":";//3 позиция 1
				txt += String.valueOf(b.ypos) + ":";//4 позиция 2
				txt += String.valueOf(b.rotate) + ":";//5 направление
				txt += String.valueOf(b.state) + ":";//6 состояние(бот или органика)
				txt += String.valueOf(b.c_red) + ":";//7 красный(режим хищников)
				txt += String.valueOf(b.c_green) + ":";//8 зеленый(режим хищников)
				txt += String.valueOf(b.c_blue) + ":";//9 синий(режим хищников)
				txt += String.valueOf(b.color.getRed()) + ":";//10 красный
				txt += String.valueOf(b.color.getGreen()) + ":";//11 зеленый
				txt += String.valueOf(b.color.getBlue()) + ":";//12 синий
				txt += String.valueOf(b.index) + ":";//13 индекс
				txt += String.valueOf(b.killed) + ":";//14 убит ли бот
				for (int i = 0; i < 64; i++) {//15 - 78 мозг
					txt += String.valueOf(b.commands[i]) + ":";
				}
				txt += ";";
			}
			try {//сохранение в файл
	            FileWriter fileWriter = new FileWriter("saved worlds/" + for_load.getText() + ".dat");
	            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	 
	            bufferedWriter.write(txt);
	 
	            bufferedWriter.close();
	        } catch (IOException ex) {//ошибка
	            System.out.println("Ошибка при записи в файл");
	            ex.printStackTrace();
	        }
		}
	}
	private class open_draw_types implements ActionListener{//открыть меню выбора режима отрисовки
		public void actionPerformed(ActionEvent e) {
			remove_main();
			add(relatives_button);
			add(clans_button);
			add(close_draw_types_button);
			menu = 1;
		}
	}
	private class close_draw_types implements ActionListener{//закрыть меню выбора режима отрисовки
		public void actionPerformed(ActionEvent e) {
			add_main();
			remove(relatives_button);
			remove(clans_button);
			remove(close_draw_types_button);
			menu = 0;
		}
	}
}
