package sct;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
import java.util.ListIterator;

public class Bot{
	ArrayList<Bot> objects;
	Random rand = new Random();
	private int x;
	private int y;
	public int xpos;
	public int ypos;
	public Color color;
	public Color starting_color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
	public int energy;
	public int minerals;
	public int killed = 0;
	public Bot[][] map;
	public int[] commands = new int[67];
	public int index = 0;
	public int age = 1000;
	public int state = 0;//бот или органика
	public int state2 = 1;//что ставить в массив с миром
	public int rotate = rand.nextInt(8);
	private int[][] movelist = {
		{0, -1},
		{1, -1},
		{1, 0},
		{1, 1},
		{0, 1},
		{-1, 1},
		{-1, 0},
		{-1, -1}
	};
	private int[] minerals_list = {
		1,
		2,
		3
	};
	private int[] photo_list = {
		13,
		10,
		8,
		6,
		5,
		4
	};
	//private int[] world_scale = {324, 216};
	private int[] world_scale = {162, 108};
	private int predators_draw_type = 2;
	public int c_red;
	public int c_green;
	public int c_blue;
	public int virus_time = 0;
	public boolean is_attacked = false;//
	public boolean is_infected = false;//
	public boolean is_share_with_me = false;//
	public int interruption = -1;//
	public Bot enr_chain_next = null;
	public Bot enr_chain_prev = null;
	public Bot self;
	public Bot(int new_xpos, int new_ypos, Color new_color, int new_energy, Bot[][] new_map, ArrayList<Bot> new_objects) {
		xpos = new_xpos;
		ypos = new_ypos;
		x = new_xpos * 10;
		y = new_ypos * 10;
		color = new_color;
		energy = new_energy;
		minerals = 0;
		objects = new_objects;
		map = new_map;
		for (int i = 0; i < 67; i++) {
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
			canvas.setColor(new Color(0, 0, 0));
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
			}else if (draw_type == 5) {//вируса
				canvas.setColor(new Color(255 / 9 * virus_time, 255 - 255 / 9 * virus_time, 0));
			}else if (draw_type == 6) {//кланов
				canvas.setColor(starting_color);
			}else if (draw_type == 7) {//родственников
				//
			}
			//canvas.fillRect(x, y, 5, 5);
			canvas.fillRect(x + 1, y + 1, 8, 8);
			if (enr_chain_next != null || enr_chain_prev != null) {
				canvas.setColor(new Color(0, 0, 0));
				canvas.fillRect(x + 3, y + 3, 4, 4);
			}
			if (enr_chain_next != null) {
				if (Math.abs(xpos - enr_chain_next.xpos) > 1) {
					int xpos_ = 0;
					if (xpos - enr_chain_next.xpos > 0) {//если я справа
						xpos_= enr_chain_next.xpos + 162;
					}else if (xpos - enr_chain_next.xpos < 0) {//если я слева
						xpos_= enr_chain_next.xpos - 162;
					}
					canvas.drawLine(x + 5, y + 5, xpos_ * 10 + 5 + (xpos - xpos_) * 5, enr_chain_next.ypos * 10 + 5 + (ypos - enr_chain_next.ypos) * 5);
				}else {
					canvas.drawLine(x + 5, y + 5, enr_chain_next.xpos * 10 + 5, enr_chain_next.ypos * 10 + 5);
				}
			}
			if (enr_chain_prev != null) {
				if (Math.abs(xpos - enr_chain_prev.xpos) > 1) {
					int xpos_ = 0;
					if (xpos - enr_chain_prev.xpos > 0) {//если я справа
						xpos_= enr_chain_prev.xpos + 162;
					}else if (xpos - enr_chain_prev.xpos < 0) {//если я слева
						xpos_= enr_chain_prev.xpos - 162;
					}
					canvas.drawLine(x + 5, y + 5, xpos_ * 10 + 5 + (xpos - xpos_) * 5, enr_chain_prev.ypos * 10 + 5 + (ypos - enr_chain_prev.ypos) * 5);
				}else {
					canvas.drawLine(x + 5, y + 5, enr_chain_prev.xpos * 10 + 5, enr_chain_prev.ypos * 10 + 5);
				}
			} 
		}else {//рисуем органику
			//canvas.setColor(new Color(90, 90, 90));
			//canvas.fillRect(x + 1, y + 1, 3, 3);
			canvas.setColor(new Color(0, 0, 0));
			canvas.fillRect(x + 1, y + 1, 8, 8);
			if (draw_type == 2) {
				int g = 255 - (int)(energy / 1000.0 * 255.0);
				if (g > 255) {
					g = 255;
				}else if (g < 0) {
					g = 0;
				}
				canvas.setColor(new Color(255, g, 0));
			}else{
				canvas.setColor(new Color(128, 128, 128));
			}
			canvas.fillRect(x + 2, y + 2, 6, 6);
		}
	}
	public int Update(ListIterator<Bot> iterator, int steps) {
		if (killed == 0) {
			if (state == 0) {//бот
				if (virus_time > 0) {
					virus_time--;
				}
				int sector = bot_in_sector();
				//energy--;
				age--;
				if (sector <= 7 & sector >= 5) {
					minerals += minerals_list[sector - 5];
				}
				update_commands(iterator);
				if (energy <= 0) {
					delete_enr_chain();
					killed = 1;
					map[xpos][ypos] = null;
					return(0);
				}else if (energy > 1000) {
					energy = 1000;
				}
				if (energy >= 800) {//автоматическое деление
					multiply(rotate, 0, iterator);
				}
				if (age <= 0) {
					delete_enr_chain();
					state = 1;
					state2 = 2;
					return(0);
				}
				if (minerals > 1000) {
					minerals = 1000;
				}
				if (interruption == -1) {
					if (is_attacked) {//если атакован
						is_attacked = false;
						interruption = index;
						index = commands[64];
					}else if (is_infected) {//если заражен
						is_infected = false;
						interruption = index;
						index = commands[65];
					}else if (is_share_with_me) {//если со мной делятся энергией
						is_share_with_me = false;
						interruption = index;
						index = commands[66];
					}
				}
				enr_chain_distribution();
				if (enr_chain_next != null && (enr_chain_next.state == 1 || enr_chain_next.killed == 1)) {
					enr_chain_next = null;
				}
				if (enr_chain_prev != null && (enr_chain_prev.state == 1 || enr_chain_prev.killed == 1)) {
					enr_chain_prev = null;
				}
			}else if (state == 1) {//падающая органика
				move(4);
				if (steps % 3 == 0) {
					energy--;
				}
				if (energy <= 0) {
					killed = 1;
					map[xpos][ypos] = null;
					return(0);
				}
			}
		}
		return(0);
	}
	public void update_commands(ListIterator<Bot> iterator) {//мозг
		for (int i = 0; i < 5; i++) {
			energy--;
			int command = commands[index];
			if (command == 0 || command == 1) {//фотосинтез
				int sector = bot_in_sector();
				if (sector <= 5) {
					energy += photo_list[sector];
					int en = photo_list[sector];
					go_green(en);
				}
				next_command_for_stop(1);
				break;
			}else if (command == 2 || command == 3) {//преобразовать минералы в энергию
				if (minerals >= 4) {
					minerals -= 4;
					energy += 16;
					go_blue(16);
				}else {
					energy += minerals * 4;
					go_blue(minerals * 4);
					minerals = 0;
				}
				next_command_for_stop(1);
				break;
			}else if (command == 4) {//походить
				if (enr_chain_next == null && enr_chain_prev == null) {
					int rot = commands[(index + 1) % 64];
					if (rot > 31) {
						rot %= 8;
					}else {
						rot = rotate;
					}
					int sens = move(rot);
					if (sens == 1) {//если удачно
						energy -= 4;
						//delete_enr_chain();
					}
				}
				next_command_for_stop(2);
				break;
			}else if (command == 5) {//атаковать
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				attack(rot, commands[(index + 2) % 64] + 1);
				//attack2(rot);
				next_command_for_stop(3);
				break;
			}else if (command == 6) {//поделиться
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				multiply(rot, 0, iterator);
				next_command_for_stop(2);
				break;
			}else if (command == 7 || command == 8) {//отдать ресурсы
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				give(rot);
				next_command_for_stop(2);
				break;
			}else if (command == 9 || command == 10) {//равномерное распределение ресурсов
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				give2(rot);
				next_command_for_stop(2);
				break;
			//
			}else if (command == 11) {//повернуть/сменить направление
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rotate += rot;
					rotate %= 8;
				}else {
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
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				index = commands[(index + 2 + see(rot)) % 64];
			}else if (command == 14) {//посмотреть на 2 клетки
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				index = commands[(index + 2 + see2(rot)) % 64];
			}else if (command == 15) {//сколько у меня энергии
				int ind = commands[(index + 1) % 64] * 15;
				if (energy >= ind) {
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 16) {//сколько у меня минералов
				int ind = commands[(index + 1) % 64] * 15;
				if (minerals >= ind) {
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 17) {//какой мой возраст
				int ind = commands[(index + 1) % 64] * 15;
				if (age >= ind) {
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 18) {//есть ли фотосинтез
				int sector = bot_in_sector();
				if (sector <= 5) {
					index = commands[(index + 1) % 64];
				}else {
					index = commands[(index + 2) % 64];
				}
			}else if (command == 19) {//есть ли приход минералов
				int sector = bot_in_sector();
				if (sector <= 7 & sector >= 5) {
					index = commands[(index + 1) % 64];
				}else {
					index = commands[(index + 2) % 64];
				}
			}else if (command == 20) {//какая моя позиция x
				double ind = commands[(index + 1) % 64] / 64.0;
				if (xpos * world_scale[0] >= ind) {
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 21) {//какая моя позиция y
				double ind = commands[(index + 1) % 64] / 64.0;
				if (ypos * world_scale[1] >= ind) {
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			}else if (command == 22) {//сколько энергии у соседа
				index = commands[(index + 2 + neighbour_param(rotate, 0, commands[(index + 1) % 64] * 15)) % 64];
			}else if (command == 23) {//сколько минералов у соседа
				index = commands[(index + 2 + neighbour_param(rotate, 1, commands[(index + 1) % 64] * 15)) % 64];
			}else if (command == 24) {//какой возраст соседа
				index = commands[(index + 2 + neighbour_param(rotate, 2, commands[(index + 1) % 64] * 15)) % 64];
			}else if (command == 25) {//какое мое направление
				index = commands[(index + rotate + 1) % 64];
			//
			}else if (command == 26) {//безусловный переход
				index = commands[(index + 1) % 64];
			}else if (command == 27) {//случайный переход
				if (rand.nextInt(64) <= commands[(index + 1) % 64]) {
					index = commands[(index + 2) % 64];
				}else {
					index = commands[(index + 3) % 64];
				}
			//
			}else if (command == 28) {//сменить цвет
				color = new Color(commands[(index + 1) % 64] * 4, commands[(index + 2) % 64] * 4, commands[(index + 3) % 64] * 4);
				energy -= 10;
				next_command_for_stop(4);
				break;
			}else if (command == 29) {//сменить один цветовой канал
				if (commands[(index + 1) % 64] % 3 == 0) {//красный
					color = new Color(commands[(index + 2) % 64] * 4, color.getGreen(), color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 1) {//зеленый
					color = new Color(color.getRed(), commands[(index + 2) % 64] * 4, color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 2) {//синий
					color = new Color(color.getRed(), color.getGreen(), commands[(index + 2) % 64] * 4);
				}
				energy -= 4;
				next_command_for_stop(3);
				break;
			}else if (command == 30) {//изменить цвет
				int r = commands[(index + 1) % 64] % 32 - 16;
				int g = commands[(index + 2) % 64] % 32 - 16;
				int b = commands[(index + 3) % 64] % 32 - 16;
				color = new Color(border(color.getRed() + r, 255, 0), border(color.getGreen() + g, 255, 0), border(color.getBlue() + b, 255, 0));
				energy -= 8;
				next_command_for_stop(4);
				break;
			}else if (command == 31) {//изменить один цветовой канал
				if (commands[(index + 1) % 64] % 3 == 0) {//красный
					color = new Color(commands[(index + 2) % 64] * 4, color.getGreen(), color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 1) {//зеленый
					color = new Color(color.getRed(), commands[(index + 2) % 64] * 4, color.getBlue());
				}else if (commands[(index + 1) % 64] % 3 == 2) {//синий
					color = new Color(color.getRed(), color.getGreen(), commands[(index + 2) % 64] * 4);
				}
				energy -= 8;
				next_command_for_stop(3);
				break;
			}else if (command == 32) {//скопировать цвет
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				copy_color(rot);
				energy -= 10;
				next_command_for_stop(2);
				break;
			//
			}else if (command == 33) {//мутация
				if (rand.nextInt(100) == 0) {
					color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
				}else {
					color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
				}
				commands[rand.nextInt(64)] = rand.nextInt(64);
				next_command_for_stop(1);
				break;
			}else if (command == 34) {//мутация соседа
				neighbour_mutate(rotate);
				next_command_for_stop(1);
				break;
			}else if (command == 35) {//самоубийство
				state = 1;
				state2 = 2;
				delete_enr_chain();
				index++;
				index %= 64;
				break;
			}else if (command == 36) {//уменьшить возраст
				age -= (commands[(index + 1) % 64] % 4) + 1;
				energy -= 4;
				next_command_for_stop(2);
				break;
			//}else if (command == 39) {//вирус
			//	int rot = commands[(index + 1) % 64];
			//	if (rot > 31) {
			//		rot %= 8;
			//	}else {
			//		rot = rotate;
			//	}
			//	virus(rot, commands[(index + 2) % 64]);
			//	energy -= 2;
			//	next_command_for_stop(3);
			//	break;
			}else if (command == 37) {//поделиться(энергетическая многоклеточная цепочка)
				int rot = commands[(index + 1) % 64];
				if (rot > 31) {
					rot %= 8;
				}else {
					rot = rotate;
				}
				multiply(rot, 1, iterator);
				next_command_for_stop(2);
				break;
			}else if (command == 38) {//какая моя позиция в энергетической многоклеточной цепочке
				if (enr_chain_next == null && enr_chain_prev == null) {//не в цепочке
					index = commands[(index + 1) % 64];
				}else if (enr_chain_next != null && enr_chain_prev != null) {//в середине
					index = commands[(index + 2) % 64];
				}else {//в конце
					index = commands[(index + 3) % 64];
				}
			}else {
				index += command;
				index %= 64;
			}
		}
	}
	public void delete_enr_chain() {
		if (enr_chain_next != null) {
			enr_chain_next.enr_chain_prev = null;
			enr_chain_next = null;
		}
		if (enr_chain_prev != null) {
			enr_chain_prev.enr_chain_next = null;
			enr_chain_prev = null;
		}
	}
	public void delete_enr_chain_victim(Bot victim) {
		if (victim.enr_chain_next != null) {
			victim.enr_chain_next.enr_chain_prev = null;
			victim.enr_chain_next = null;
		}
		if (victim.enr_chain_prev != null) {
			victim.enr_chain_prev.enr_chain_next = null;
			victim.enr_chain_prev = null;
		}
	}
	public void enr_chain_distribution() {
		int sum = energy;
		int count = 1;
		if (enr_chain_next != null) {
			sum += enr_chain_next.energy;
			count++;
		}
		if (enr_chain_prev != null) {
			sum += enr_chain_prev.energy;
			count++;
		}
		if (count > 1) {
			energy = sum / count;
			if (enr_chain_next != null) {
				enr_chain_next.energy = sum / count;
			}
			if (enr_chain_prev != null) {
				enr_chain_prev.energy = sum / count;
			}
		}
	}
	public void next_command_for_stop(int count) {
		if (interruption > -1) {
			index = interruption;
			interruption = -1;
		}else {
			index += count;
			index %= 64;
		}
	}
	public void neighbour_mutate(int rot) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {
					Bot b = map[pos[0]][pos[1]];
					if (b != null) {
						if (rand.nextInt(100) == 0) {
							b.color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
						}else {
							b.color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
						}
						b.commands[rand.nextInt(64)] = rand.nextInt(64);
					}
				}
			}
		}
	}
	public int neighbour_param(int rot, int type, int ind) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				Bot b = map[pos[0]][pos[1]];
				if (b != null) {
					int np = 0;
					if (type == 0) {
						np = b.energy;
					}else if (type == 1) {
						np = b.minerals;
					}else if (type == 2) {
						np = b.age;
					}
					if (np >= ind) {
						return(0);
					}else {
						return(1);
					}
				}
			}else {
				return(2);
			}
		}else {
			return(2);
		}
		return(2);
	}
	public void virus(int rot, int virus_pos) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] > 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {
					Bot victim = map[pos[0]][pos[1]];
					virus_time = 9;
					for (int i = 0; i < 7; i++) {
						victim.commands[(virus_pos + i) % 64] = commands[Math.floorMod(index - 4 + i, 64)];
					}
					if (rand.nextInt(8) == 0) {
						int ind = rand.nextInt(6);
						if (ind >= 4) {
							ind++;
						}
						victim.commands[(virus_pos + ind) % 64] = rand.nextInt(64);
					}
					victim.is_infected = true;
				}
			}
		}
	}
	public void go_red(int en) {
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
	public void go_green(int en) {
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
	public void go_blue(int en) {
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
	public void copy_color(int rot) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {
					color = new Color(map[pos[0]][pos[1]].color.getRed(), map[pos[0]][pos[1]].color.getGreen(), map[pos[0]][pos[1]].color.getBlue());
				}
			}
		}
	}
	public int see(int rot) {
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
	public int see2(int rot) {
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
	public void give(int rot) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {
					Bot relative = map[pos[0]][pos[1]];
					if (relative.killed == 0) {
						relative.is_share_with_me = true;
						relative.energy += energy / 4;
						relative.minerals += minerals / 4;
						energy -= energy / 4;
						minerals -= minerals / 4;
					}
				}
			}
		}
	}
	public void give2(int rot) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				if (map[pos[0]][pos[1]].state == 0) {
					Bot relative = map[pos[0]][pos[1]];
					if (relative.killed == 0) {
						relative.is_share_with_me = true;
						int enr = relative.energy + energy;
						int mnr = relative.minerals + minerals;
						relative.energy = enr / 2;
						relative.minerals = mnr / 2;
						energy = enr / 2;
						minerals = mnr / 2;
					}
				}
			}
		}
	}
	public void attack2(int rot) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				Bot victim = map[pos[0]][pos[1]];
				if (victim != null) {
					victim.killed = 1;
					energy += victim.energy;
					map[pos[0]][pos[1]] = null;
					delete_enr_chain_victim(victim);
					go_red(victim.energy);
					if (victim.state == 0) {
						victim.is_attacked = true;
					}
				}
			}
		}
	}
	public void attack(int rot, int strength) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] != null) {
				Bot victim = map[pos[0]][pos[1]];
				if (victim != null) {
					int en = 0;
					if (victim.energy >= strength) {
						energy += strength;
						victim.energy -= strength;
						en = strength;
					}else {
						energy += victim.energy;
						en = victim.energy;
						victim.energy = 0;
						victim.killed = 1;
						map[pos[0]][pos[1]] = null;
						delete_enr_chain_victim(victim);
					}
					go_red(en);
					if (victim.state == 0) {
						victim.is_attacked = true;
					}
				}
			}
		}
	}
	public boolean is_relative(Color color1, Color color2) {
		boolean is_red = (color1.getRed() - 20 < color2.getRed()) && (color1.getRed() + 20 > color2.getRed());
		boolean is_green = (color1.getGreen() - 20 < color2.getGreen()) && (color1.getGreen() + 20 > color2.getGreen());
		boolean is_blue = (color1.getBlue() - 20 < color2.getBlue()) && (color1.getBlue() + 20 > color2.getBlue());
		return(is_red && is_green && is_blue);
	}
	public int[] get_rotate_position(int rot){
		int[] pos = new int[2];
		pos[0] = (xpos + movelist[rot][0]) % world_scale[0];
		pos[1] = ypos + movelist[rot][1];
		if (pos[0] < 0) {
			pos[0] = world_scale[0] - 1;
		}else if(pos[0] >= world_scale[0]) {
			pos[0] = 0;
		}
		return(pos);
	}
	public int[] get_rotate_position_chain(int rot){
		int[] pos = new int[2];
		pos[0] = (x + 5) + movelist[rot][0] * 4;
		pos[1] = (y + 5) + movelist[rot][1] * 4;
		return(pos);
	}
	public int[] get_rotate_position2cells(int rot){
		int[] pos = new int[2];
		pos[0] = (xpos + movelist[rot][0] * 2) % world_scale[0];
		pos[1] = ypos + movelist[rot][1] * 2;
		if (pos[0] < 0) {
			pos[0] += world_scale[0];
		}else if(pos[0] >= world_scale[0]) {
			pos[0] = 0;
		}
		return(pos);
	}
	public int move(int rot) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] == null) {
				Bot self = map[xpos][ypos];
				map[xpos][ypos] = null;
				xpos = pos[0];
				ypos = pos[1];
				x = xpos * 10;
				y = ypos * 10;
				map[xpos][ypos] = self;
				return(1);
			}
		}
		return(0);
	}
	public void multiply(int rot, int chain, ListIterator<Bot> iterator) {
		int[] pos = get_rotate_position(rot);
		if (pos[1] >= 0 & pos[1] < world_scale[1]) {
			if (map[pos[0]][pos[1]] == null) {
				energy -= 150;
				if (energy <= 0) {
					killed = 1;
					map[xpos][ypos] = null;
				}else { 
					Color new_color;
					if (rand.nextInt(100) == 0) {
						new_color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					}else {
						new_color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
					}
					//
					int[] new_brain = new int[67];
					for (int i = 0; i < 64; i++) {
						new_brain[i] = commands[i];
					}
					//
					if (rand.nextInt(4) == 0) {//мутация
						new_brain[rand.nextInt(67)] = rand.nextInt(64);
					}
					//
					Bot new_bot;
					new_bot = new Bot(pos[0], pos[1], new_color, energy / 2, map, objects);
					new_bot.self = new_bot;
					new_bot.minerals = minerals / 2;
					energy /= 2;
					minerals /= 2;
					new_bot.commands = new_brain;
					new_bot.starting_color = starting_color;
					map[pos[0]][pos[1]] = new_bot;
					iterator.add(new_bot);
					//
					if (enr_chain_next == null && chain == 1) {
						enr_chain_next = new_bot;
						new_bot.enr_chain_prev = self;
					}
					if (enr_chain_prev == null && chain == 1) {
						enr_chain_prev = new_bot;
						new_bot.enr_chain_next = self;
					}
					//
					if (rand.nextInt(100) == 0) {
						color = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
					}else {
						color = new Color(border(color.getRed() + rand.nextInt(-12, 12), 255, 0), border(color.getGreen() + rand.nextInt(-12, 12), 255, 0), border(color.getBlue() + rand.nextInt(-12, 12), 255, 0));
					}
					if (rand.nextInt(4) == 0) {//мутация предка
						commands[rand.nextInt(64)] = rand.nextInt(64);
					}
				}
			}
		}
	}
	public int bot_in_sector() {
		int sec = ypos / (world_scale[1] / 8);
		if (sec > 7) {
			sec = 10;
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
