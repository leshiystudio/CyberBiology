package sct;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
import java.util.ListIterator;

public class Bot{
	ArrayList<Bot> objects;//ссылка на массив ботов
	Random rand = new Random();
	private int x;//позиция в пикселях
	private int y;
	public int xpos;//позиция в клетках
	public int ypos;
	public Color color;//цвет
	public Color starting_color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));//клан
	public int energy;//энергия
	public int minerals = 0;//минералы
	public int killed = 0;//убит ли бот?
	public Bot[][] map;//ссылка на карту
	public int[][] org_map;//сылка на карту органики
	public int interruptions_count = 18;//количество прерываний
	public int[] commands = new int[64 + interruptions_count];
	public int index = 0;//индекс
	public int age = 1500;//сколько осталось жить
	public int state = 0;//бот или органика
	public int state2 = 1;//что ставить в массив с миром
	public int rotate = rand.nextInt(8);//направление
	public int memory = 0;
	private int[][] movelist = {//куда ходить
		{0, -1},
		{1, -1},
		{1, 0},
		{1, 1},
		{0, 1},
		{-1, 1},
		{-1, 0},
		{-1, -1}
	};
	private int[] minerals_list = {1, 2, 3, 4};//сколько бот получит минералов
	private int[] photo_list = {13, 10, 8, 6, 5, 4};//сколько бот получит энергии от фотосинтеза
	//private int[] world_scale = {324, 216};
	private int[] world_scale = {324, 216};//размер мира
	private int predators_draw_type = 1;//вариант режима отрисовки хищников
	public int c_red;//красный в режиме отрисовки хищников
	public int c_green;//зеленый в режиме отрисовки хищников
	public int c_blue;//синий в режиме отрисовки хищников
	public int c_yellow;//желтый в режиме отрисовки хищников
	public boolean[] interruptions_list = new boolean[interruptions_count];
	public int interruption = -1;//выполняется ли сейчас прерывание
	public Bot self;//я
	public Bot next = null;//следующий в цепочке
	public Bot prev = null;//предыдущий в цепочке
	public Bot(int new_xpos, int new_ypos, Color new_color, int new_energy, Bot[][] new_map, int[][] new_org_map, ArrayList<Bot> new_objects) {//некоторые данные передаются в конструктор
		xpos = new_xpos;
		ypos = new_ypos;
		x = new_xpos * 5;
		y = new_ypos * 5;
		color = new_color;
		energy = new_energy;
		objects = new_objects;
		map = new_map;
		org_map = new_org_map;
		for (int i = 0; i < 64 + interruptions_count; i++) {
			commands[i] = rand.nextInt(64);
		}
		if (predators_draw_type == 0 || predators_draw_type == 2) {
			c_red = 0;
			c_green = 0;
			c_blue = 0;
		}else if (predators_draw_type == 1) {
			c_red = 128;
			c_green = 128;
			c_blue = 128;
		}
	}
	public void Draw(Graphics canvas, int draw_type) {
		if (state == 0) {//рисуем бота
			//canvas.setColor(new Color(0, 0, 0));//черное окаймление
			//canvas.fillRect(x, y, 10, 10);
			if (draw_type == 0) {//режим отрисовки хищников
				if (predators_draw_type == 0 || predators_draw_type == 2) {
					int r = 0;
					int g = 0;
					int b = 0;
					int all = c_red + c_green + c_blue + c_yellow;
					if (all == 0) {
						r = 128;
						g = 128;
						b = 128;
					}else {
						int y = (int)((c_yellow * 1.0) / all * 255.0);
						r = max((int)(c_red * 1.0 / all * 255.0), y);
						g = max((int)(c_green * 1.0 / all * 255.0), y);
						b = (int)((c_blue * 1.0) / all * 255.0);
					}
					canvas.setColor(new Color(r, g, b));
				}else if (predators_draw_type == 1) {
					canvas.setColor(new Color(c_red, c_green, c_blue));
				}
			}else if (draw_type == 1) {//цвета
				canvas.setColor(color);
			}else if (draw_type == 2) {//энергии
				int g = 255 - (int)(energy / 1000.0 * 255.0);
				if (g > 255) {
					g = 255;
				}else if (g < 0) {
					g = 0;
				}
				canvas.setColor(new Color(255, g, 0));
			}else if (draw_type == 3) {//минералов
				int g = 255 - (int)(minerals / 1000.0 * 255.0);
				if (g > 255) {
					g = 255;
				}else if (g < 0) {
					g = 0;
				}
				canvas.setColor(new Color(0, g, 255));
			}else if (draw_type == 4) {//возраста
				canvas.setColor(new Color((int)(age / 1500.0 * 255.0), (int)(age / 1500.0 * 255.0), 255 - (int)(age / 1500.0 * 255.0)));
			}else if (draw_type == 5) {//памяти
				int m = border(memory, 63, 0);
				canvas.setColor(new Color(0, 255 - m * 4, m * 4));
			}else if (draw_type == 6) {//кланов
				canvas.setColor(starting_color);
			}else if (draw_type == 7) {//цепочек
				if (next == null && prev == null) {
					canvas.setColor(new Color(255, 255, 128));
				}else if (next != null && prev != null) {
					canvas.setColor(new Color(200, 0, 200));
				}else {
					canvas.setColor(new Color(85, 0, 85));
				}
			}
			canvas.fillRect(x, y, 5, 5);
			//canvas.fillRect(x + 1, y + 1, 8, 8);
			//рисование связей между ботами
			//if (next != null || prev != null) {//если бот в цепоке, рисуем на нем квадрат
			//	canvas.setColor(new Color(0, 0, 0));
			//	//canvas.fillRect(x + 3, y + 3, 4, 4);
			//	canvas.fillRect(x + 1, y + 1, 3, 3);
			//}
			//if (next != null) {//если есть следующий
			//	if (Math.abs(xpos - next.xpos) > 1) {//если расстояние между ботами больше 1(если они по разные стороны мира)
			//		int xpos_ = 0;
			//		if (xpos - next.xpos > 0) {//если я справа
			//			xpos_= next.xpos + 162;
			//		}else if (xpos - next.xpos < 0) {//если я слева
			//			xpos_= next.xpos - 162;
			//		}
			//		canvas.drawLine(x + 5, y + 5, xpos_ * 10 + 5 + (xpos - xpos_) * 5, next.ypos * 10 + 5 + (ypos - next.ypos) * 5);
			//	}else {//иначе просто рисовать цепочку
			//		canvas.drawLine(x + 5, y + 5, next.xpos * 10 + 5, next.ypos * 10 + 5);
			//	}
			//}
			//if (prev != null) {//если есть предыдущий
			//	if (Math.abs(xpos - prev.xpos) > 1) {//если расстояние между ботами больше 1(если они по разные стороны мира)
			//		int xpos_ = 0;
			//		if (xpos - prev.xpos > 0) {//если я справа
			//			xpos_= prev.xpos + 162;
			//		}else if (xpos - prev.xpos < 0) {//если я слева
			//			xpos_= prev.xpos - 162;
			//		}
			//		canvas.drawLine(x + 5, y + 5, xpos_ * 10 + 5 + (xpos - xpos_) * 5, prev.ypos * 10 + 5 + (ypos - prev.ypos) * 5);
			//	}else {//иначе просто рисовать цепочку
			//		canvas.drawLine(x + 5, y + 5, prev.xpos * 10 + 5, prev.ypos * 10 + 5);
			//	}
			//}
		}else {//рисуем органику
			//canvas.setColor(new Color(0, 0, 0));//черное окаймление
			//canvas.fillRect(x + 1, y + 1, 8, 8);
			if (draw_type == 2) {//в режиме отрисовки энергии у органики отображается количество энергии
				int g = 255 - (int)(energy / 1000.0 * 255.0);
				if (g > 255) {
					g = 255;
				}else if (g < 0) {
					g = 0;
				}
				canvas.setColor(new Color(255, g, 0));
			}else{//во всех остальных случаях она просто серая
				//canvas.setColor(new Color(128, 128, 128));
				canvas.setColor(new Color(128, 60, 0));
			}
			//canvas.fillRect(x + 2, y + 2, 6, 6);
			canvas.fillRect(x + 1, y + 1, 3, 3);
		}
	}
	public int Update(ListIterator<Bot> iterator, int steps) {//обновление бота
		if (killed == 0) {//если бот не мертв
			if (state == 0) {//бот
				age--;//постареть
				int sector = bot_in_sector();//для минералов
				if (sector >= 4) {//приход минералов
					minerals += minerals_list[sector - 4];
				}
				update_commands(iterator);//мозг
				if (energy <= 0) {//если мало энергии, умереть(органика не появляется)
					delete_chain();//удалить связи в цепочке
					killed = 1;
					map[xpos][ypos] = null;
					return(0);
				}else if (energy > 1000) {//ограничитель количества энергии
					energy = 1000;
				}
				if (energy >= 800) {//автоматическое деление
					multiply(rotate, 0, iterator);
				}
				if (age <= 0 || org_map[xpos][ypos] >= 800) {//если пора помирать от старости
					delete_chain();//удалить связи в цепочке
					state = 1;//стать органикой
					state2 = 2;
					org_to_map((int)(energy * 0.3));//ложим 30% энергии в почву
					energy -= (int)(energy * 0.3);//уменьшаем энергию
					return(0);
				}
				if (minerals > 1000) {//ограничитель минералов
					minerals = 1000;
				}
				interruptions();//прерывания
				update_chain();//многоклеточные цепочки
			}else if (state == 1) {//органика тоже бот
				int[] pos = get_rotate_position(4);
				if (pos[1] >= 0 & pos[1] < world_scale[1]) {
					if (map[pos[0]][pos[1]] == null) {//если внизу свободно, падать
						move(4);
					}else {//если внизу занято, сыпаться
						int[] pos_left = get_rotate_position(5);//клетка слева снизу
						int[] pos_right = get_rotate_position(3);//клетка справа снизу
						if (map[pos_left[0]][pos_left[1]] == null && map[pos_right[0]][pos_right[1]] != null) {//сыпаться влево
							move(5);
						}else if (map[pos_left[0]][pos_left[1]] != null && map[pos_right[0]][pos_right[1]] == null) {//сыпаться вправо
							move(3);
						}else if (map[pos_left[0]][pos_left[1]] == null && map[pos_right[0]][pos_right[1]] == null) {//сыпаться в случайную сторону
							move(3 + rand.nextInt(2) * 2);
						}
					}
				}
				if (steps % 3 == 0) {//каждые 3 хода органика тратит энергию
					energy--;
					org_map[xpos][ypos]++;
				}
				if (energy <= 0) {//если энергии нет, органика сгнила
					killed = 1;
					map[xpos][ypos] = null;
					return(0);
				}
			}
		}
		return(0);
	}
	public void update_commands(ListIterator<Bot> iterator) {//мозг
		for (int i = 0; i < 5; i++) {//выполняется макс. 5 команд
			energy--;//за каждую выполненную команду -1 энергии
			int command = commands[index];//команда
			if (command == 0 || command == 1) {//фотосинтез
				int sector = bot_in_sector();
				if (sector <= 5) {//если бот высоко, то получает энергию из массива для фотосинтеза
					int mnr = commands[(index + 1) % 64] % 5;
					if (minerals < mnr) {
						mnr = minerals;
					}
					minerals -= mnr;
					int en = (int)(photo_list[sector] * (2 * (1 + mnr * 0.25) * (org_map[xpos][ypos] / 1000 * 2.65 + 0.35)));
					energy += en;
					if (mnr == 0 || (mnr != 0 && minerals == 0)) {//если не усиливаем фотосинтез
						go_green(en);//зеленеем
					}else {
						go_cyan(en);//зеленеем и синеем
					}
					if (mnr > 0) {
						go_blue((int)(photo_list[sector] * (mnr * 0.25)));//синеем
					}
				}else {
					interruptions_list[2] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 2 || command == 3) {//преобразовать минералы в энергию
				if (minerals >= 4) {//1 минерал == 3 энергии, за раз можно переработать только 4 минерала
					minerals -= 4;
					energy += 12;
					go_blue(12);//синеем
				}else if (minerals > 0){
					energy += minerals * 3;
					go_blue(minerals * 3);//синеем
					minerals = 0;
				}else {//если нет минералов, выполняется прерывание
					interruptions_list[3] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 4) {//походить
				if (next == null && prev == null) {//боты в цепочке ходить не могут
					int rot = get_rotate_from_genome((index + 1) % 64);//направление
					boolean sens = move(rot);
					if (sens) {//если удачно
						energy -= 4;
						//delete_enr_chain();
					}else {
						interruptions_list[4] = true;//если неудачно - выполняется прерывание
					}
				}else {//
					interruptions_list[4] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 5) {//атаковать
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				boolean sens = attack(rot, commands[(index + 2) % 64] + 1);
				if (!sens) {
					interruptions_list[5] = true;//если неудачно - выполняется прерывание
				}
				//attack2(rot);
				next_command_for_stop(3);
				break;//завершающая
			}else if (command == 6) {//поделиться
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				boolean sens = multiply(rot, 0, iterator);
				if (!sens) {
					interruptions_list[6] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 7 || command == 8) {//отдать ресурсы (шанс команды увеличен)
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				boolean sens = give(rot);
				if (!sens) {
					interruptions_list[7] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 9 || command == 10) {//равномерное распределение ресурсов (шанс команды увеличен)
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				boolean sens = give2(rot);
				if (!sens) {
					interruptions_list[8] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			//
			}else if (command == 11) {//повернуть/сменить направление
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {//если параметр < 32, то бот поворачивает
					rotate += rot;
					rotate %= 8;
				}else {//иначе сразу устанавливает направление в нужное
					rotate = rot % 8;
				}
				index += 2;
				index %= 64;
			}else if (command == 12) {//установить направление в случайное
				rotate = rand.nextInt(8);
				index += 1;
				index %= 64;
			//
			}else if (command == 13) {//посмотреть
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				index = commands[(index + 2 + see(rot)) % 64];
			}else if (command == 14) {//посмотреть на 2 клетки
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				index = commands[(index + 2 + see2(rot)) % 64];
			}else if (command == 15) {//сколько у меня энергии
				int ind = commands[(index + 1) % 64] * 15;
				if (energy >= ind) {//если энергии больше, чем параметр * 15, то переходим по 1 переходу, иначе по 2
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 16) {//сколько у меня минералов
				int ind = commands[(index + 1) % 64] * 15;
				if (minerals >= ind) {//если минералов больше, чем параметр * 15, то переходим по 1 переходу, иначе по 2
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 17) {//какой мой возраст
				int ind = commands[(index + 1) % 64] * 15;
				if (age >= ind) {//если возраст больше, чем параметр * 15, то переходим по 1 переходу, иначе по 2
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 18) {//есть ли фотосинтез
				int sector = bot_in_sector();
				if (sector <= 5) {//если есть, переходим по 1 переходу, иначе по 2
					index = commands[(index + 1) % 64];
				}else {
					index = commands[(index + 2) % 64];
				}
			}else if (command == 19) {//есть ли приход минералов
				int sector = bot_in_sector();
				if (sector <= 7 & sector >= 4) {//если есть, переходим по 1 переходу, иначе по 2
					index = commands[(index + 1) % 64];
				}else {
					index = commands[(index + 2) % 64];
				}
			}else if (command == 20) {//какая моя позиция x
				double ind = commands[(index + 1) % 64] / 64.0;
				if (xpos * 1.0 / world_scale[0] * 63 >= ind) {//если позиция x, приведенная к числу от 0 до 63 больше параметра, переходим по 1 переходу, иначе по 2
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 21) {//какая моя позиция y
				double ind = commands[(index + 1) % 64] / 64.0;
				if (ypos * 1.0 / world_scale[0] * 63 >= ind) {//если позиция y, приведенная к числу от 0 до 63 больше параметра, переходим по 1 переходу, иначе по 2
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 22) {//сколько энергии у соседа
				int sens = neighbour_param(get_rotate_from_genome((index + 1) % 64), 0, commands[(index + 2) % 64] * 15);
				if (sens != 2) {
					index = commands[(index + 3 + sens) % 64];
				}else {
					index += 5;
					index %= 64;
					interruptions_list[9] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 23) {//сколько минералов у соседа
				int sens = neighbour_param(get_rotate_from_genome((index + 1) % 64), 1, commands[(index + 2) % 64] * 15);
				if (sens != 2) {
					index = commands[(index + 3 + sens) % 64];
				}else {
					index += 5;
					index %= 64;
					interruptions_list[9] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 24) {//какой возраст соседа
				int sens = neighbour_param(get_rotate_from_genome((index + 1) % 64), 2, commands[(index + 2) % 64] * 15);
				if (sens != 2) {
					index = commands[(index + 3 + sens) % 64];
				}else {
					index += 5;
					index %= 64;
					interruptions_list[9] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 25) {//какое мое направление
				index = commands[(index + rotate + 1) % 64];//переходим по n переходу, где n - направление бота
			//
			}else if (command == 26) {//безусловный переход
				index = commands[(index + 1) % 64];//просто переходим по параметру
			}else if (command == 27) {//случайный переход
				if (rand.nextInt(64) <= commands[(index + 1) % 64]) {//переходим по первому или второму переходам с шансом из параметра
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			//
			}else if (command == 28) {//сменить цвет
				color = new Color(commands[(index + 1) % 64] * 4, commands[(index + 2) % 64] * 4, commands[(index + 3) % 64] * 4);//цвет немного изменяется, тратя 10 энергии
				energy -= 10;
				next_command_for_stop(4);
				break;//завершающая
			}else if (command == 29) {//сменить один цветовой канал
				//1 цветовой канал немного изменяется, тратя 4 энергии
				if (commands[(index + 1) % 64] % 3 == 0) {//красный
					color = new Color(commands[(index + 2) % 64] * 4, color.getGreen(), color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 1) {//зеленый
					color = new Color(color.getRed(), commands[(index + 2) % 64] * 4, color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 2) {//синий
					color = new Color(color.getRed(), color.getGreen(), commands[(index + 2) % 64] * 4);
				}
				energy -= 4;
				next_command_for_stop(3);
				break;//завершающая
			}else if (command == 30) {//изменить цвет
				int r = commands[(index + 1) % 64] % 32 - 16;
				int g = commands[(index + 2) % 64] % 32 - 16;
				int b = commands[(index + 3) % 64] % 32 - 16;
				color = new Color(border(color.getRed() + r, 255, 0), border(color.getGreen() + g, 255, 0), border(color.getBlue() + b, 255, 0));//цвет полностью меняется, тратя 8 энергии
				energy -= 8;
				next_command_for_stop(4);
				break;//завершающая
			}else if (command == 31) {//изменить один цветовой канал
				//1 цветовой канал меняется полностью, тратя 8 энергии
				if (commands[(index + 1) % 64] % 3 == 0) {//красный
					color = new Color(commands[(index + 2) % 64] * 4, color.getGreen(), color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 1) {//зеленый
					color = new Color(color.getRed(), commands[(index + 2) % 64] * 4, color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 2) {//синий
					color = new Color(color.getRed(), color.getGreen(), commands[(index + 2) % 64] * 4);
				}
				energy -= 8;
				next_command_for_stop(3);
				break;//завершающая
			}else if (command == 32) {//скопировать цвет
				//мимикрия
				//бот копирует цвет соседа, тратя 10 энергии
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				boolean sens = copy_color(rot);
				if (!sens) {
					interruptions_list[10] = true;//если неудачно - выполняется прерывание
				}
				energy -= 10;
				next_command_for_stop(2);
				break;//завершающая
			//
			}else if (command == 33) {//мутация
				//бот мутирует. цвет также немного меняется и с шансом 1/800 меняется полностью
				if (rand.nextInt(800) == 0) {
					color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
				}else {
					color = new Color(border(color.getRed() + rand.nextInt(-12, 13), 255, 0), border(color.getGreen() + rand.nextInt(-12, 13), 255, 0), border(color.getBlue() + rand.nextInt(-12, 13), 255, 0));
				}
				commands[rand.nextInt(64 + interruptions_count)] = rand.nextInt(64);
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 34) {//мутация соседа
				//бот мутирует соседа
				boolean sens = neighbour_mutate(get_rotate_from_genome((index + 1) % 64));
				if (!sens) {
					interruptions_list[11] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 35) {//самоубийство
				//бот превращается в органику
				state = 1;
				state2 = 2;
				delete_chain();//удалить связи в цепочке
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 36) {//уменьшить возраст
				//бот уменьшает возраст, тратя 4 энергии
				age -= (commands[(index + 1) % 64] % 4) + 1;
				energy -= 4;
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 37) {//поделиться(энергетическая многоклеточная цепочка)
				//бот делится, добавляя потомка в многоклеточную цепочку. Если у бота уже есть 2 связи в цепочке, потомок появляутся свободным
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				boolean sens = multiply(rot, 1, iterator);
				if (!sens) {
					interruptions_list[12] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 38) {//какая моя позиция в энергетической многоклеточной цепочке
				if (next == null && prev == null) {//не в цепочке
					index = commands[(index + 1) % 64];
				}else if (next != null && prev != null) {//в середине
					index = commands[(index + 2) % 64];
				}else {//в конце
					index = commands[(index + 3) % 64];
				}
			}else if (command == 39) {//энергия соседа больше моей
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				int sens = neighbour_param_bigger_than_my_param(rot, 0);
				if (sens != 2) {
					index = commands[(index + 2 + sens) % 64];
				}else {
					index += 4;
					index %= 64;
					interruptions_list[13] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 40) {//минералов соседа больше моих
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				int sens = neighbour_param_bigger_than_my_param(rot, 1);
				if (sens != 2) {
					index = commands[(index + 2 + sens) % 64];
				}else {
					index += 4;
					index %= 64;
					interruptions_list[13] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 41) {//возраст соседа больше моего
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				int sens = neighbour_param_bigger_than_my_param(rot, 2);
				if (sens != 2) {
					index = commands[(index + 2 + sens) % 64];
				}else {
					index += 4;
					index %= 64;
					interruptions_list[13] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 42) {//собирать органику под собой
				int org = commands[(index + 1) % 64] + 1;//сколько органики собираем
				if (org > org_map[xpos][ypos]) {
					org = org_map[xpos][ypos];
				}
				if (org_map[xpos][ypos] > 0) {//если что - то собрали, желтеем
					go_yellow(org);
				}else {
					interruptions_list[14] = true;//если неудачно - выполняется прерывание
				}
				org_map[xpos][ypos] -= org;//съедаем органику
				energy += org;
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 43) {//собирать органику перед собой собой
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				int[] pos = get_rotate_position(rot);
				if (pos[1] >= 0 & pos[1] < world_scale[1]) {//проверка на границы карты
					int org = commands[(index + 2) % 64] + 1;//сколько органики собираем
					if (org > org_map[pos[0]][pos[1]]) {
						org = org_map[pos[0]][pos[1]];
					}
					if (org_map[pos[0]][pos[1]] > 0) {//если что - то собрали, желтеем
						go_yellow(org);
					}else {
						interruptions_list[15] = true;//если неудачно - выполняется прерывание
					}
					org_map[pos[0]][pos[1]] -= org;//съедаем органику
					energy += org;
				}else {
					interruptions_list[15] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(3);
				break;//завершающая
			}else if (command == 44) {//сколько органики подо мной
				int org = org_map[xpos][ypos];//уровень органики
				if (org >= commands[(index + 1) % 64] * 15) {//если органики больше параметра, переходим по первому переходу, иначе по второму
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 45) {//сколько органики передо мной
				int rot = get_rotate_from_genome((index + 1) % 64);//направление
				int[] pos = get_rotate_position(rot);
				if (pos[1] >= 0 & pos[1] < world_scale[1]) {//проверка на границы карты
					int org = org_map[pos[0]][pos[1]];//уровень органики
					if (org >= commands[(index + 2) % 64] * 15) {//если органики больше параметра, переходим по первому переходу, иначе по второму
						index = commands[(index + 3) % 64];
					}else {
						index = commands[(index + 4) % 64];
					}
				}else {
					interruptions_list[16] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 46) {//логические операции с памятью
				int oper = commands[(index + 1) % 64] % 3;
				int param = commands[(index + 2) % 64];
				if (oper == 0) {//>
					if (memory > param) {
						index = commands[(index + 3) % 64];
					}else {
						index = commands[(index + 4) % 64];
					}
				}else if (oper == 1) {//==
					if (memory == param) {
						index = commands[(index + 3) % 64];
					}else {
						index = commands[(index + 4) % 64];
					}
				}else if (oper == 2) {//>=
					if (memory >= param) {
						index = commands[(index + 3) % 64];
					}else {
						index = commands[(index + 4) % 64];
					}
				}
			}else if (command == 47) {//арифметические операции с памятью
				int oper = commands[(index + 1) % 64] % 2;
				int param = commands[(index + 2) % 64];
				if (oper == 0) {//+
					memory += param;
					if (memory > 63) {
						memory = 63;
					}
				}else if (oper == 1) {//-
					memory -= param;
					if (memory < 0) {
						memory = 0;
					}
				}
				index += 3;
				index %= 64;
			}else if (command == 48) {//записать число в память
				int param1 = commands[(index + 1) % 64] % 8;
				if (param1 == 0) {//число
					memory = commands[(index + 2) % 64];
				}else if (param1 == 1) {//энергию
					memory = (int)(energy / 1000.0 * 63);
				}else if (param1 == 2) {//минералы
					memory = (int)(minerals / 1000.0 * 63);
				}else if (param1 == 3) {//возраст
					memory = (int)(age / 1500.0 * 63);
				}else if (param1 == 4) {//направление
					memory = rotate * 9;
				}else if (param1 == 5) {//x - позицию
					memory = (int)(xpos * 1.0 / world_scale[0] * 63);
				}else if (param1 == 6) {//y - позицию
					memory = (int)(ypos * 1.0 / world_scale[0] * 63);
				}else if (param1 == 7) {//случайное число
					memory = rand.nextInt(64);
				}
				index += 3;
				index %= 64;
			}else if (command == 49) {//cколько соседей вокруг
				int ind = commands[(index + 1) % 64] % 8;
				int param = commands[(index + 2) % 64] % 3;
				int c = count_of_neighbours(param);
				if (c > ind) {
					index = commands[(index + 3) % 64];
				}else if (c < ind) {
					index = commands[(index + 4) % 64];
				}else {
					index = commands[(index + 5) % 64];
				}
			}else {//безусловный переход
				index += command;
				index %= 64;
			}
		}
	}
	//функции команд генома
	public int count_of_neighbours(int param) {//количество соседей
		int c = 0;
		for (int i = 0; i < 8; i++) {
			int[] pos = get_rotate_position(i);
			if (pos[1] >= 0 & pos[1] < world_scale[1]) {
				if (map[pos[0]][pos[1]] != null) {
					if (param == 0) {
						c++;
					}else if (param == 1) {
						if (map[pos[0]][pos[1]].state == 0) {
							c++;
						}
					}else if (param == 2) {
						if (map[pos[0]][pos[1]].state == 1) {
							c++;
						}
					}
				}
			}
		}
		return(c);
	}
	public int neighbour_param_bigger_than_my_param(int rot, int type) {//параметр соседа больше моего
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {//если на карте бот
					Bot b = map[pos[0]][pos[1]];
					if (b.killed == 0) {
						int param1 = 0;
						int param2 = 0;
						if (type == 0) {//энергия
							param1 = b.energy;
							param2 = energy;
						}else if (type == 1) {//минералы
							param1 = b.minerals;
							param2 = minerals;
						}else if (type == 2) {//возраст
							param1 = b.age;
							param2 = age;
						}
						if (param1 >= param2) {//если больше, вернуть 0
							return(0);
						}else {//если меньше, вернуть 1
							return(1);
						}
					}
				}
			}
		}
		return(2);//недачно
	}
	public boolean neighbour_mutate(int rot) {//мутация соседа
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {//если на карте бот
					Bot b = map[pos[0]][pos[1]];
					if (b.killed == 0) {
						if (rand.nextInt(800) == 0) {//меняем боту цвет
							b.color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
						}else {
							b.color = new Color(border(color.getRed() + rand.nextInt(-12, 13), 255, 0), border(color.getGreen() + rand.nextInt(-12, 13), 255, 0), border(color.getBlue() + rand.nextInt(-12, 13), 255, 0));
						}
						b.commands[rand.nextInt(64 + interruptions_count)] = rand.nextInt(64);//меняем мозг
						return(true);//успешно
					}
				}
			}
		}
		return(false);//нет
	}
	public int neighbour_param(int rot, int type, int ind) {//сколько чего - либо у соседа
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				Bot b = map[pos[0]][pos[1]];
				if (b.killed == 0) {
					int np = 0;
					if (type == 0) {//энергии
						np = b.energy;
					}else if (type == 1) {//минералов
						np = b.minerals;
					}else if (type == 2) {//возраста
						np = b.age;
					}
					if (np >= ind) {//если данных больше параметра, вернуть 0, иначе вернуть 1(параметр сразу умножается на 15)
						return(0);
					}else {
						return(1);
					}
				}
			}
		}
		return(2);//если нет соседа, ошибка и все остальное, вернуть 2
	}
	public boolean copy_color(int rot) {//скопировать цвет
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {//если есть сосед, копируем его цвет
					color = new Color(map[pos[0]][pos[1]].color.getRed(), map[pos[0]][pos[1]].color.getGreen(), map[pos[0]][pos[1]].color.getBlue());
					return(true);//успешно
				}
			}
		}
		return(false);//нет
	}
	public int see(int rot) {//посмотреть
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] == null) {
				return(1);//если ничего
			}else if (map[pos[0]][pos[1]].state == 0) {//если бот
				Bot b = map[pos[0]][pos[1]];
				if (b != null) {
					if (is_relative(color, b.color)) {
						return(3);//если родственник
					}else {
						return(2);//если враг
					}
				}else {
					return(1);//если ничего
				}
			}else if (map[pos[0]][pos[1]].state != 0) {
				return(4);//если органика
			}
		}else {
			return(0);//если граница
		}
		return(0);//если ошибка
	}
	public int see2(int rot) {//посмотреть на 2 клетки
		int[] pos = get_rotate_position2cells(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] == null) {
				return(1);//если ничего
			}else if (map[pos[0]][pos[1]].state == 0) {//если бот
				Bot b = map[pos[0]][pos[1]];
				if (b != null) {
					if (is_relative(color, b.color)) {
						return(3);//если родственник
					}else {
						return(2);//если враг
					}
				}else {
					return(1);//если ничего
				}
			}else if (map[pos[0]][pos[1]].state != 0) {
				return(4);//если органика
			}
		}else {
			return(0);//если граница
		}
		return(0);//если ошибка
	}
	public boolean give(int rot) {//отдать часть энергии соседу
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {//если есть сосед
					Bot relative = map[pos[0]][pos[1]];
					if (relative.killed == 0) {//если сосед не мертв
						relative.interruptions_list[1] = true;//ставим флаг прерывания
						relative.energy += energy / 4;//отдать соседу 1/4 энергии и минералов
						relative.minerals += minerals / 4;
						energy -= energy / 4;
						minerals -= minerals / 4;
						return(true);//успешно
					}
				}
			}
		}
		return(false);//нет
	}
	public boolean give2(int rot) {//равномерное распределение ресурсов
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {//если есть сосед
					Bot relative = map[pos[0]][pos[1]];
					if (relative.killed == 0) {//если сосед не мертв
						relative.interruptions_list[1] = true;//ставим флаг прерывания
						int enr = relative.energy + energy;//энергия и минералы распределяются равномерно
						int mnr = relative.minerals + minerals;
						relative.energy = enr / 2;
						relative.minerals = mnr / 2;
						energy = enr / 2;
						minerals = mnr / 2;
						return(true);//успешно
					}
				}
			}
		}
		return(false);//нет
	}
	public boolean attack2(int rot) {//убить
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {//если есть сосед
				Bot victim = map[pos[0]][pos[1]];
				if (victim.killed == 0) {
					victim.killed = 1;//сосед убит
					energy += victim.energy;//едим его энергию
					map[pos[0]][pos[1]] = null;//чистим карту
					victim.delete_chain();//удаляем соседу связи в цепочке
					go_red(victim.energy);//краснеем
					return(true);//успешно
				}
			}
		}
		return(false);//нет
	}
	public boolean attack(int rot, int strength) {//укусить
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {//если есть сосед
				Bot victim = map[pos[0]][pos[1]];
				if (victim.killed == 0) {
					int en = 0;
					int strength2 = (int)(strength * 1.3);//общая сила укуса(+ 30% идет в почву органикой)
					if (victim.energy >= strength2) {//если у соседа достаточно энергии, забрать часть себе
						energy += strength;
						org_to_map(strength2 - strength);//часть энергии идет в почву органикой
						victim.energy -= strength2;
						en = strength;
					}else {//если у соседа мало энергии, он умирает
						energy += victim.energy;
						en = victim.energy;
						victim.energy = 0;
						victim.killed = 1;
						map[pos[0]][pos[1]] = null;
						victim.delete_chain();
					}
					go_red(en);//краснеем
					if (victim.state == 0) {//ставим флаг прерывания
						victim.interruptions_list[0] = true;
					}
					return(true);//успешно
				}
			}
		}
		return(false);//нет
	}
	public boolean move(int rot) {//двигаться
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] == null) {//если можно походить
				map[xpos][ypos] = null;//сбросить текущую позицию
				xpos = pos[0];//ставим новую позицию
				ypos = pos[1];
				x = xpos * 5;
				y = ypos * 5;
				map[xpos][ypos] = self;//поставить себя в карту
				return(true);//успешно
			}
		}
		return(false);//нет
	}
	public boolean multiply(int rot, int chain, ListIterator<Bot> iterator) {//деление
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] == null) {//если можно поделиться
				energy -= 150;//деление тратит 150 энергии
				if (energy <= 0) {//если столько нет, бот умрет
					killed = 1;
					map[xpos][ypos] = null;
				}else { //если энергии хватает
					Color new_color;//цвет нового бота
					if (rand.nextInt(800) == 0) {//немного меняем(с шансом 1/100 - полностью)
						new_color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					}else {
						new_color = new Color(border(color.getRed() + rand.nextInt(-12, 13), 255, 0), border(color.getGreen() + rand.nextInt(-12, 13), 255, 0), border(color.getBlue() + rand.nextInt(-12, 13), 255, 0));
					}
					if (rand.nextInt(800) == 0) {//меняем свой цвет
						color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					}else {
						color = new Color(border(color.getRed() + rand.nextInt(-12, 13), 255, 0), border(color.getGreen() + rand.nextInt(-12, 13), 255, 0), border(color.getBlue() + rand.nextInt(-12, 13), 255, 0));
					}
					//копируем мозг
					int[] new_brain = new int[64 + interruptions_count];
					for (int i = 0; i < 64 + interruptions_count; i++) {
						new_brain[i] = commands[i];
					}
					//
					if (rand.nextInt(4) == 0) {//мутация потомка
						new_brain[rand.nextInt(64 + interruptions_count)] = rand.nextInt(64);
					}
					if (rand.nextInt(4) == 0) {//мутация предка
						commands[rand.nextInt(64 + interruptions_count)] = rand.nextInt(64);
						interruptions_list[17] = true;//прерывание
					}
					//
					Bot new_bot;//новый бот
					new_bot = new Bot(pos[0], pos[1], new_color, energy / 2, map, org_map, objects);
					new_bot.self = new_bot;//записываем потомка в потомка :0
					new_bot.minerals = minerals / 2;//энергия и минералы равномерно распределяются
					energy /= 2;
					minerals /= 2;
					new_bot.commands = new_brain;//мозг нового бота
					new_bot.starting_color = starting_color;//клан
					map[pos[0]][pos[1]] = new_bot;//пускаем потомка в мир
					iterator.add(new_bot);
					//добавление потомка в цепочку(только, если chain_type > 0)
					if (chain != 0) {
						if (next == null) {
							next = new_bot;
							new_bot.prev = self;
						}else if (prev == null) {
							prev = new_bot;
							new_bot.next = self;
						}
					}
					//иначе бот будет свободным
					return(true);//успешно
				}
			}
		}
		return(false);//нет
	}
	//технические фукнкции
	public void org_to_map(int en) {//положить органику в карту в квадрате 3*3
		int org = (int)(en / 9.0);
		for (int i = 0; i < 8; i++) {//проходим по соседним клеткам
			int[] pos = get_rotate_position(i);
			if (pos[1] >= 0 & pos[1] < world_scale[1]) {//проверка на границы карты
				org_map[pos[0]][pos[1]] += org;//добавляем органику в карту
			}
		}
		org_map[xpos][ypos] += org;//добавляем органику под собой
	}
	public int get_rotate_from_genome(int ind) {//получение направления
		int rot = commands[(ind + 1) % 64];//направление берется из параметра
		if (rot > 21) {
			rot %= 8;
		}else if (rot < 42){//или из бота, если параметр > 31
			rot = rotate;
		}else {
			rot = memory % 8;
		}
		return(rot);
	}
	public void update_chain() {//обновление цепочек
		chain_distribution();//распределение энергии в цепочке
		//если сосед по цепочке помер, а ссылки не стерлись, стереть их
		if (next != null && (next.state == 1 || next.killed == 1)) {
			next = null;
		}
		if (prev != null && (prev.state == 1 || prev.killed == 1)) {
			prev = null;
		}
	}
	public void interruptions() {//обработка прерываний
		if (interruption == -1) {//прерывания(если выполнилось нужное условие, выполняется прерывание. Индекс бота сохраняется, и устанавливается в значение из генов прерывания(они в геноме после мозга). После выполнения завершающей команды индекс восстанавливается.)
			for (int i = 0; i < interruptions_count; i++) {
				if (interruptions_list[i]) {
					interruption = index;
					index = commands[64 + i];
					break;
				}
			}
		}
	}
	public void delete_chain() {//удалить связи в цепочке
		if (next != null) {//если есть следующий
			next.prev = null;//у него стираем ссылку на себя
			next = null;//стираем ссылку на следующего
		}
		if (prev != null) {//если есть предыдущий
			prev.next = null;//у него стираем ссылку на себя
			prev = null;//стираем ссылку на предыдущего
		}
	}
	public void chain_distribution() {//распределение ресурсов в цепочке
		int sum = energy;//энергии
		int sum2 = minerals;//минералов
		int count = 1;//подсчет связанных ботов
		if (next != null) {
			sum += next.energy;
			sum2 += next.minerals;
			count++;
		}
		if (prev != null) {
			sum += prev.energy;
			sum2 += prev.minerals;
			count++;
		}
		if (count > 1) {//распределение ресурсов
			energy = sum / count;
			minerals = sum2 / count;
			if (next != null) {//со следующим
				next.energy = sum / count;
				next.minerals = sum2 / count;
			}
			if (prev != null) {//с предыдущим
				prev.energy = sum / count;
				prev.minerals = sum2 / count;
			}
		}
	}
	public void next_command_for_stop(int count) {//остановка выполнения прерывания при выполнении завершающей команды
		if (interruption > -1) {
			index = interruption;
			interruption = -1;
			interruptions_list = new boolean[interruptions_count];
		}else {
			index += count;
			index %= 64;
		}
	}
	public void go_red(int en) {//краснеть
		if (predators_draw_type == 0) {
			c_red++;
		}else if (predators_draw_type == 1) {
			if (c_red == 128 && c_green == 128 && c_blue == 128) {
				c_red = 255;
				c_green = 0;
				c_blue = 0;
			}else {
				c_red = border(c_red + 3, 255, 0);
				c_green = border(c_green - 3, 255, 0);
				c_blue = border(c_blue - 3, 255, 0);
			}
		}else if (predators_draw_type == 2) {
			c_red += en;
		}
	}
	public void go_green(int en) {//зеленеть
		if (predators_draw_type == 0) {
			c_green++;
		}else if (predators_draw_type == 1) {
			if (c_red == 128 && c_green == 128 && c_blue == 128) {
				c_red = 0;
				c_green = 255;
				c_blue = 0;
			}else {
				c_red = border(c_red - 3, 255, 0);
				c_green = border(c_green + 3, 255, 0);
				c_blue = border(c_blue - 3, 255, 0);
			}
		}else if (predators_draw_type == 2) {
			c_green += en;
		}
	}
	public void go_blue(int en) {//синеть
		if (predators_draw_type == 0) {
			c_blue++;
		}else if (predators_draw_type == 1) {
			if (c_red == 128 && c_green == 128 && c_blue == 128) {
				c_red = 0;
				c_green = 0;
				c_blue = 255;
			}else {
				c_red = border(c_red - 3, 255, 0);
				c_green = border(c_green - 3, 255, 0);
				c_blue = border(c_blue + 3, 255, 0);
			}
		}else if (predators_draw_type == 2) {
			c_blue += en;
		}
	}
	public void go_yellow(int en) {//желтеть
		if (predators_draw_type == 0) {
			c_yellow++;
		}else if (predators_draw_type == 1) {
			if (c_red == 128 && c_green == 128 && c_blue == 128) {
				c_red = 255;
				c_green = 255;
				c_blue = 0;
			}else {
				c_red = border(c_red + 3, 255, 0);
				c_green = border(c_green + 3, 255, 0);
				c_blue = border(c_blue - 3, 255, 0);
			}
		}else if (predators_draw_type == 2) {
			c_yellow += en;
		}
	}
	public void go_cyan(int en) {//синеть зеленеть
		if (predators_draw_type == 0) {
			c_blue++;
			c_green++;
		}else if (predators_draw_type == 1) {
			if (c_red == 128 && c_green == 128 && c_blue == 128) {
				c_red = 0;
				c_green = 255;
				c_blue = 255;
			}else {
				c_red = border(c_red - 3, 255, 0);
				c_green = border(c_green + 3, 255, 0);
				c_blue = border(c_blue + 3, 255, 0);
			}
		}else if (predators_draw_type == 2) {
			c_blue += en;
			c_green += en;
		}
	}
	public boolean is_relative(Color color1, Color color2) {//являются ли боты родственниками(если все цветовые каналы 2 бота находятся в диапазоне от -20 до 20 относительно моего цвета)
		boolean is_red = (color1.getRed() - 20 < color2.getRed()) && (color1.getRed() + 20 > color2.getRed());
		boolean is_green = (color1.getGreen() - 20 < color2.getGreen()) && (color1.getGreen() + 20 > color2.getGreen());
		boolean is_blue = (color1.getBlue() - 20 < color2.getBlue()) && (color1.getBlue() + 20 > color2.getBlue());
		return(is_red && is_green && is_blue);
	}
	public int[] get_rotate_position(int rot){//позиция по направлению
		int[] pos = new int[2];
		pos[0] = (xpos + movelist[rot][0]) % world_scale[0];//зацикленный мир
		pos[1] = ypos + movelist[rot][1];
		if (pos[0] < 0) {//еще
			pos[0] = world_scale[0] - 1;
		}else if(pos[0] >= world_scale[0]) {
			pos[0] = 0;
		}
		return(pos);
	}
	public int[] get_rotate_position2cells(int rot){//позици по направлению на 2 клетки
		int[] pos = new int[2];
		pos[0] = (xpos + movelist[rot][0] * 2) % world_scale[0];//зацикленный мир
		pos[1] = ypos + movelist[rot][1] * 2;
		if (pos[0] < 0) {//еще
			pos[0] += world_scale[0];
		}else if(pos[0] >= world_scale[0]) {
			pos[0] = 0;
		}
		return(pos);
	}
	public int bot_in_sector() {//для фотосинтеза и минералов
		int sec = ypos / (world_scale[1] / 8);
		if (sec > 7) {
			sec = 7;
		}
		return(sec);
	}
	public int border(int number, int border1, int border2) {//число, верхняя граница, нижняя граница
		if (number > border1) {
			number = border1;
		}else if (number < border2) {
			number = border2;
		}
		return(number);
	}
	public int max(int number1, int number2) {//максимальное из двух чисел
		if (number1 > number2) {
			return(number1);
		}else if (number2 > number1) {
			return(number2);
		}else {
			return(number1);
		}
	}
}
