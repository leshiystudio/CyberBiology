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
	public int[] commands = new int[64 + 13];
	public int index = 0;//индекс
	public int age = 1000;//сколько осталось жить
	public int state = 0;//бот или органика
	public int state2 = 1;//что ставить в массив с миром
	public int rotate = rand.nextInt(8);//направление
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
	private int[] minerals_list = {1, 2, 3};//сколько бот получит минералов
	private int[] photo_list = {13, 10, 8, 6, 5, 4};//сколько бот получит энергии от фотосинтеза
	//private int[] world_scale = {324, 216};
	private int[] world_scale = {162, 108};//размер мира
	private int predators_draw_type = 2;//вариант режима отрисовки хищников
	public int c_red;//красный в режиме отрисовки хищников
	public int c_green;//зеленый в режиме отрисовки хищников
	public int c_blue;//синий в режиме отрисовки хищников
	public boolean[] interruptions_list = {false, false, false, false, false, false, false, false, false, false, false, false, false};
	public int interruption = -1;//выполняется ли сейчас прерывание
	public Bot enr_chain_next = null;//следующий в цепочке
	public Bot enr_chain_prev = null;//предыдущий в цепочке
	public Bot self;//я
	public Bot[][] chain = new Bot[2][2];//многоклеточные цепочки. 0 - энергетическая, 1 - минеральная. 0 - следующий, 1 - предыдущий
	public Bot(int new_xpos, int new_ypos, Color new_color, int new_energy, Bot[][] new_map, ArrayList<Bot> new_objects) {//некоторые данные передаются в конструктор
		xpos = new_xpos;
		ypos = new_ypos;
		x = new_xpos * 10;
		y = new_ypos * 10;
		color = new_color;
		energy = new_energy;
		objects = new_objects;
		map = new_map;
		for (int i = 0; i < 64 + 13; i++) {
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
	public void Draw(Graphics canvas, int draw_type, int chain_draw_type) {
		if (state == 0) {//рисуем бота
			canvas.setColor(new Color(0, 0, 0));//черное окаймление
			canvas.fillRect(x, y, 10, 10);
			if (draw_type == 0) {//режим отрисовки хищников
				if (predators_draw_type == 0 || predators_draw_type == 2) {
					int r = 0;
					int g = 0;
					int b = 0;
					if (c_red + c_green + c_blue == 0) {
						r = 128;
						g = 128;
						b = 128;
					}else {
						r = (int)((c_red * 1.0) / (c_red + c_green + c_blue) * 255.0);
						g = (int)((c_green * 1.0) / (c_red + c_green + c_blue) * 255.0);
						b = (int)((c_blue * 1.0) / (c_red + c_green + c_blue) * 255.0);
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
				canvas.setColor(new Color((int)(age / 1000.0 * 255.0), (int)(age / 1000.0 * 255.0), 255 - (int)(age / 1000.0 * 255.0)));
			}else if (draw_type == 5) {//
				//
			}else if (draw_type == 6) {//кланов
				canvas.setColor(starting_color);
			}else if (draw_type == 7) {//
				//
			}
			//canvas.fillRect(x, y, 5, 5);
			canvas.fillRect(x + 1, y + 1, 8, 8);
			//рисование связей между ботами
			if (chain_draw_type != 0) {//рисуем многоклеточные цепочки
				int chain_type = chain_draw_type - 1;
				if (chain[chain_type][0] != null || chain[chain_type][1] != null) {//если бот в цепоке, рисуем на нем квадрат
					canvas.setColor(new Color(0, 0, 0));
					canvas.fillRect(x + 3, y + 3, 4, 4);
				}
				if (chain[chain_type][0] != null) {//если есть следующий
					if (Math.abs(xpos - chain[chain_type][0].xpos) > 1) {//если расстояние между ботами больше 1(если они по разные стороны мира)
						int xpos_ = 0;
						if (xpos - chain[chain_type][0].xpos > 0) {//если я справа
							xpos_= chain[chain_type][0].xpos + 162;
						}else if (xpos - chain[chain_type][0].xpos < 0) {//если я слева
							xpos_= chain[chain_type][0].xpos - 162;
						}
						canvas.drawLine(x + 5, y + 5, xpos_ * 10 + 5 + (xpos - xpos_) * 5, chain[chain_type][0].ypos * 10 + 5 + (ypos - chain[chain_type][0].ypos) * 5);
					}else {//иначе просто рисовать цепочку
						canvas.drawLine(x + 5, y + 5, chain[chain_type][0].xpos * 10 + 5, chain[chain_type][0].ypos * 10 + 5);
					}
				}
				if (chain[chain_type][1] != null) {//если есть предыдущий
					if (Math.abs(xpos - chain[chain_type][1].xpos) > 1) {//если расстояние между ботами больше 1(если они по разные стороны мира)
						int xpos_ = 0;
						if (xpos - chain[chain_type][1].xpos > 0) {//если я справа
							xpos_= chain[chain_type][1].xpos + 162;
						}else if (xpos - chain[chain_type][1].xpos < 0) {//если я слева
							xpos_= chain[chain_type][1].xpos - 162;
						}
						canvas.drawLine(x + 5, y + 5, xpos_ * 10 + 5 + (xpos - xpos_) * 5, chain[chain_type][1].ypos * 10 + 5 + (ypos - chain[chain_type][1].ypos) * 5);
					}else {//иначе просто рисовать цепочку
						canvas.drawLine(x + 5, y + 5, chain[chain_type][1].xpos * 10 + 5, chain[chain_type][1].ypos * 10 + 5);
					}
				}
			}
		}else {//рисуем органику
			//canvas.setColor(new Color(90, 90, 90));
			//canvas.fillRect(x + 1, y + 1, 3, 3);
			canvas.setColor(new Color(0, 0, 0));//черное окаймление
			canvas.fillRect(x + 1, y + 1, 8, 8);
			if (draw_type == 2) {//в режиме отрисовки энергии у органики отображается количество энергии
				int g = 255 - (int)(energy / 1000.0 * 255.0);
				if (g > 255) {
					g = 255;
				}else if (g < 0) {
					g = 0;
				}
				canvas.setColor(new Color(255, g, 0));
			}else{//во всех остальных случаях она просто серая
				canvas.setColor(new Color(128, 128, 128));
			}
			canvas.fillRect(x + 2, y + 2, 6, 6);
		}
	}
	public int Update(ListIterator<Bot> iterator, int steps) {//обновление бота
		if (killed == 0) {//если бот не мертв
			if (state == 0) {//бот
				age--;//постареть
				int sector = bot_in_sector();//для минералов
				if (sector <= 7 & sector >= 5) {//приход минералов
					minerals += minerals_list[sector - 5];
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
				if (age <= 0) {//если пора помирать от старости
					delete_chain();//удалить связи в цепочке
					state = 1;//стать органикой
					state2 = 2;
					return(0);
				}
				if (minerals > 1000) {//ограничитель минералов
					minerals = 1000;
				}
				interruptions();
				update_chain();
			}else if (state == 1) {//органика тоже бот
				move(4);//падать
				if (steps % 3 == 0) {//каждые 3 хода органика тратит энергию
					energy--;
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
					energy += photo_list[sector];
					int en = photo_list[sector];
					go_green(en);//зеленеем
				}else {
					interruptions_list[2] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 2 || command == 3) {//преобразовать минералы в энергию
				if (minerals >= 4) {//1 минерал == 4 энергии, за раз можно переработать только 4 минерала
					minerals -= 4;
					energy += 16;
					go_blue(16);//синеем
				}else if (minerals > 0){
					energy += minerals * 4;
					go_blue(minerals * 4);//синеем
					minerals = 0;
				}else {//если нет минералов, выполняется прерывание
					interruptions_list[3] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 4) {//походить
				if (chain[0][0] == null && chain[0][1] == null && chain[1][0] == null && chain[1][1] == null) {//боты в цепочке ходить не могут
					int rot = commands[(index + 1) % 64];//направление берется из параметра
					if (rot > 31) {
						rot %= 8;
					}else {//или из бота, если параметр > 31
						rot = rotate;
					}
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
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
				boolean sens = attack(rot, commands[(index + 2) % 64] + 1);
				if (!sens) {
					interruptions_list[5] = true;//если неудачно - выполняется прерывание
				}
				//attack2(rot);
				next_command_for_stop(3);
				break;//завершающая
			}else if (command == 6) {//поделиться
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
				boolean sens = multiply(rot, 0, iterator);
				if (!sens) {
					interruptions_list[6] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 7 || command == 8) {//отдать ресурсы (шанс команды увеличен)
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
				boolean sens = give(rot);
				if (!sens) {
					interruptions_list[7] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 9 || command == 10) {//равномерное распределение ресурсов (шанс команды увеличен)
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
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
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
				index = commands[(index + 2 + see(rot)) % 64];
			}else if (command == 14) {//посмотреть на 2 клетки
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
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
				if (sector <= 7 & sector >= 5) {//если есть, переходим по 1 переходу, иначе по 2
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
				int sens = neighbour_param(rotate, 0, commands[(index + 1) % 64] * 15);
				if (sens != 2) {
					index = commands[(index + 2 + sens) % 64];
				}else {
					interruptions_list[9] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 23) {//сколько минералов у соседа
				int sens = neighbour_param(rotate, 1, commands[(index + 1) % 64] * 15);
				if (sens != 2) {
					index = commands[(index + 2 + sens) % 64];
				}else {
					interruptions_list[9] = true;//если неудачно - выполняется прерывание
				}
			}else if (command == 24) {//какой возраст соседа
				int sens = neighbour_param(rotate, 2, commands[(index + 1) % 64] * 15);
				if (sens != 2) {
					index = commands[(index + 2 + sens) % 64];
				}else {
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
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
				boolean sens = copy_color(rot);
				if (!sens) {
					interruptions_list[10] = true;//если неудачно - выполняется прерывание
				}
				energy -= 10;
				next_command_for_stop(2);
				break;//завершающая
			//
			}else if (command == 33) {//мутация
				//бот мутирует. цвет также немного меняется и с шансом 1/100 меняется полностью
				if (rand.nextInt(100) == 0) {
					color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
				}else {
					color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
				}
				commands[rand.nextInt(64 + 13)] = rand.nextInt(64);
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 34) {//мутация соседа
				//бот мутирует соседа
				boolean sens = neighbour_mutate(rotate);
				if (!sens) {
					interruptions_list[11] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 35) {//самоубийство
				//бот превращается в органику
				state = 1;
				state2 = 2;
				delete_chain();//удалить связи в цепочке
				next_command_for_stop(1);
				break;//завершающая
			}else if (command == 36) {//уменьшить возраст
				//бот уменьшает возраст, тратя 1 энергии
				age -= (commands[(index + 1) % 64] % 4) + 1;
				energy -= 4;
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 37) {//поделиться(энергетическая многоклеточная цепочка)
				//бот делится, добавляя потомка в многоклеточную цепочку. Если у бота уже есть 2 связи в цепочке, потомок появляутся свободным
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
				boolean sens = multiply(rot, 1, iterator);
				if (!sens) {
					interruptions_list[12] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 38) {//поделиться(минеральная многоклеточная цепочка)
				//бот делится, добавляя потомка в многоклеточную цепочку. Если у бота уже есть 2 связи в цепочке, потомок появляутся свободным
				int rot = commands[(index + 1) % 64];//направление берется из параметра
				if (rot > 31) {
					rot %= 8;
				}else {//или из бота, если параметр > 31
					rot = rotate;
				}
				boolean sens = multiply(rot, 2, iterator);
				if (!sens) {
					interruptions_list[12] = true;//если неудачно - выполняется прерывание
				}
				next_command_for_stop(2);
				break;//завершающая
			}else if (command == 39) {//какая моя позиция в энергетической многоклеточной цепочке
				if (chain[0][0] == null && chain[0][1] == null) {//не в цепочке
					index = commands[(index + 1) % 64];
				}else if (chain[0][0] != null && chain[0][1] != null) {//в середине
					index = commands[(index + 2) % 64];
				}else {//в конце
					index = commands[(index + 3) % 64];
				}
			}else {//безусловный переход
				index += command;
				index %= 64;
			}
		}
	}
	//функции команд генома
	public boolean neighbour_mutate(int rot) {//мутация соседа
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {//если на карте бот
					Bot b = map[pos[0]][pos[1]];
					if (b.killed == 0) {
						if (rand.nextInt(100) == 0) {//меняем боту цвет
							b.color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
						}else {
							b.color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
						}
						b.commands[rand.nextInt(64 + 13)] = rand.nextInt(64);//меняем мозг
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
			}else {//если нет соседа, ошибка и все остальное, вернуть 2
				return(2);
			}
		}else {
			return(2);
		}
		return(2);
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
					if (victim.energy >= strength) {//если у соседа достаточно энергии, забрать часть себе
						energy += strength;
						victim.energy -= strength;
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
				x = xpos * 10;
				y = ypos * 10;
				map[xpos][ypos] = self;//поставить себя в карту
				return(true);//успешно
			}
		}
		return(false);//нет
	}
	public boolean multiply(int rot, int chain_type, ListIterator<Bot> iterator) {//деление
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] == null) {//если можно поделиться
				energy -= 150;//деление тратит 150 энергии
				if (energy <= 0) {//если столько нет, бот умрет
					killed = 1;
					map[xpos][ypos] = null;
				}else { //если энергии хватает
					Color new_color;//цвет нового бота
					if (rand.nextInt(100) == 0) {//немного меняем(с шансом 1/100 - полностью)
						new_color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					}else {
						new_color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
					}
					if (rand.nextInt(100) == 0) {//меняем свой цвет
						color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					}else {
						color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
					}
					//копируем мозг
					int[] new_brain = new int[64 + 13];
					for (int i = 0; i < 64 + 13; i++) {
						new_brain[i] = commands[i];
					}
					//
					if (rand.nextInt(4) == 0) {//мутация потомка
						new_brain[rand.nextInt(64 + 13)] = rand.nextInt(64);
					}
					if (rand.nextInt(4) == 0) {//мутация предка
						commands[rand.nextInt(64 + 13)] = rand.nextInt(64);
					}
					//
					Bot new_bot;//новый бот
					new_bot = new Bot(pos[0], pos[1], new_color, energy / 2, map, objects);
					new_bot.self = new_bot;//записываем потомка в потомка :0
					new_bot.minerals = minerals / 2;//энергия и минералы равномерно распределяются
					energy /= 2;
					minerals /= 2;
					new_bot.commands = new_brain;//мозг нового бота
					new_bot.starting_color = starting_color;//клан
					map[pos[0]][pos[1]] = new_bot;//пускаем потомка в мир
					iterator.add(new_bot);
					//добавление потомка в цепочку(только, если chain_type > 0)
					if (chain_type != 0) {
						if (chain[chain_type - 1][0] == null) {//если нет ссылки на следующего
							chain[chain_type - 1][0] = new_bot;//добавляем
							new_bot.chain[chain_type - 1][1] = self;
						}else if (chain[chain_type - 1][1] == null) {//если нет ссылки на предыдущего
							chain[chain_type - 1][1] = new_bot;//добавляем
							new_bot.chain[chain_type - 1][0] = self;
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
	public void update_chain() {//обновление цепочек
		chain_distribution();//распределение энергии в цепочке
		//если сосед по цепочке помер, а ссылки не стерлись, стереть их
		for (int i = 0; i < 2; i++) {
			if (chain[i][0] != null && (chain[i][0].state == 1 || chain[i][0].killed == 1)) {
				chain[i][0] = null;
			}
			if (chain[i][1] != null && (chain[i][1].state == 1 || chain[i][1].killed == 1)) {
				chain[i][1] = null;
			}
		}
	}
	public void interruptions() {//обработка прерываний
		if (interruption == -1) {//прерывания(если выполнилось нужное условие, выполняется прерывание. Индекс бота сохраняется, и устанавливается в значение из генов прерывания(они в геноме после мозга). После выполнения завершающей команды индекс восстанавливается.)
			for (int i = 0; i < 13; i++) {
				if (interruptions_list[i]) {
					interruption = index;
					index = commands[64 + i];
					break;
				}
			}
		}
	}
	public void delete_chain() {//удалить связи в цепочке
		for (int i = 0; i < 2; i++) {
			if (chain[i][0] != null) {//если есть следующий
				chain[i][0].chain[0][1] = null;//у него стираем ссылку на себя
				chain[i][0] = null;//стираем ссылку на следующего
			}
			if (chain[i][1] != null) {//если есть предыдущий
				chain[i][1].chain[0][0] = null;//у него стираем ссылку на себя
				chain[i][1] = null;//стираем ссылку на предыдущего
			}
		}
	}
	public void chain_distribution() {//распределение ресурсов в цепочке
		int sum = energy;//энергии
		int count = 1;//подсчет связанных ботов
		if (chain[0][0] != null) {
			sum += chain[0][0].energy;
			count++;
		}
		if (chain[0][1] != null) {
			sum += chain[0][1].energy;
			count++;
		}
		if (count > 1) {//распределение энергии
			energy = sum / count;
			if (chain[0][0] != null) {//со следующим
				chain[0][0].energy = sum / count;
			}
			if (chain[0][1] != null) {//с предыдущим
				chain[0][1].energy = sum / count;
			}
		}
		
		sum = minerals;//минералов
		count = 1;//подсчет связанных ботов
		if (chain[1][0] != null) {
			sum += chain[1][0].minerals;
			count++;
		}
		if (chain[1][1] != null) {
			sum += chain[1][1].minerals;
			count++;
		}
		if (count > 1) {//распределение энергии
			minerals = sum / count;
			if (chain[1][0] != null) {//со следующим
				chain[1][0].minerals = sum / count;
			}
			if (chain[1][1] != null) {//с предыдущим
				chain[1][1].minerals = sum / count;
			}
		}
	}
	public void next_command_for_stop(int count) {//остановка выполнения прерывания при выполнении завершающей команды
		if (interruption > -1) {
			index = interruption;
			interruption = -1;
			interruptions_list = new boolean[13];
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
				c_red = border(c_red + 25, 255, 0);
				c_green = border(c_green - 25, 255, 0);
				c_blue = border(c_blue - 25, 255, 0);
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
				c_red = border(c_red - 25, 255, 0);
				c_green = border(c_green + 25, 255, 0);
				c_blue = border(c_blue - 25, 255, 0);
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
				c_red = border(c_red - 25, 255, 0);
				c_green = border(c_green - 25, 255, 0);
				c_blue = border(c_blue + 25, 255, 0);
			}
		}else if (predators_draw_type == 2) {
			c_blue += en;
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
