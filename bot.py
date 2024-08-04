import organics
from random import randint as rand
import image_factory
import pygame
from game_object import GameObject
pygame.init()

class Bot(GameObject):
    def __init__(self, pos, color, world, objects, bots, energy=1000, draw_type=0):
        GameObject.__init__(self, pos, image_factory.get_image(color))
        self.name = "bot"
        self.killed = 0
        self.color = color
        self.rotate = rand(0, 7)
        self.energy = energy
        self.age = 1000
        self.world = world
        self.objects = objects
        self.index = 0
        self.commands = [rand(0, 63) for x in range(64)]
        self.minerals = 0
        self.attack_count = 0
        self.photo_count = 0
        self.minerals_count = 0
        self.bots = bots
        self.photo_list = [
            10,
            8,
            6,
            4,
            3,
            1
            ]
        self.minerals_list = [
            1,
            2,
            3
            ]
        self.last_draw_type = [0]
        self.change_image(draw_type)

    def next_command(self, i=1):
        self.index += i
        self.index %= 64

    def bot_in_sector(self):
        sector_len = int(self.world_scale[1] / 8)
        error = self.world_scale[1] - sector_len * 8
        sec = int(self.pos[1] / sector_len)
        if sec > 7:
            return(10)
        return(sec)

    def change_image(self, draw_type):
        if draw_type == 0:
            self.image = image_factory.get_image(self.color)
        elif draw_type == 1:
            g = 255 - int((self.energy / 1000) * 255)
            if g < 0:
                g = 0
            try:
                self.image = image_factory.get_image((255, g, 0))
            except:
                print(g)
        elif draw_type == 2:
            rg = 255 - int((self.minerals / 1000) * 255)
            if rg < 0:
                rg = 0
            self.image = image_factory.get_image(
                (
                    rg,
                    rg,
                    255
                    )
                )
        elif draw_type == 3:
            self.image = image_factory.get_image(
                (
                    int((self.age / 1000) * 255),
                    int((self.age / 1000) * 255),
                    int((self.age / 1000) * 255)
                    )
                )
        elif draw_type == 4:
            count = sum((self.photo_count, self.attack_count, self.minerals_count))
            if count == 0:
                R = 128
                G = 128
                B = 128
            else:
                R = int(self.attack_count / count * 255)
                G = int(self.photo_count / count * 255)
                B = int(self.minerals_count / count * 255)
            self.image = image_factory.get_image((R, G, B))

    def multiply(self, draw_type, rotate):#поделиться
        self.energy -= 150
        if self.energy <= 0:
            self.killed = 1
            self.world[self.pos[0]][self.pos[1]] = "none"
            self.kill()
        else:
            pos2 = [
                (self.pos[0] + GameObject.movelist[rotate][0]) % self.world_scale[0],
                self.pos[1] + GameObject.movelist[rotate][1]
                ]
            if pos2[1] >= 0 and pos2[1] <= self.border - 1:
                if self.world[pos2[0]][pos2[1]] == "none":
                    new_bot = Bot(pos2, self.color, self.world, self.objects, self.bots, energy=int(self.energy * 0.5), draw_type=draw_type)
                    self.energy = int(self.energy * 0.5)
                    if rand(1, 4) == 1:
                        new_color = (
                            rand(0, 255),
                            rand(0, 255),
                            rand(0, 255)
                            )
                        new_commands = self.commands.copy()
                        new_commands[rand(0, 63)] = rand(0, 63)
                        new_bot.color = new_color
                        new_bot.commands = new_commands
                        new_bot.minerals = int(self.minerals * 0.5)
                        self.minerals = int(self.minerals * 0.5)
                    else:
                        new_bot.commands = self.commands.copy()
                    self.objects.add(new_bot)
                    self.world[pos2[0]][pos2[1]] = "bot"

    def get_rotate_position(self, rotate):#вычисление координат на которые смотрит бот
        pos = [
            (self.pos[0] + GameObject.movelist[rotate][0]) % self.world_scale[0],
            self.pos[1] + GameObject.movelist[rotate][1]
            ]
        return(pos)

    def attack(self, pos):#атаковать
        if pos[1] >= 0 and pos[1] <= self.border - 1:
            if self.world[pos[0]][pos[1]] == "bot" or self.world[pos[0]][pos[1]] == "organics":
                victim = None
                for victim in self.objects:
                    if victim.pos == pos:
                        break
                    else:
                        victim = None
                if victim != None:
                    self.energy += victim.energy
                    victim.killed = 1
                    victim.kill()
                    self.attack_count += 1
            self.world[pos[0]][pos[1]] = "none"

    def give(self, pos):
        friend = None
        for friend in self.objects:
            if friend.pos == pos and friend.name == "bot":
                break
            else:
                friend = None
        if friend != None:
            friend.energy += int(self.energy / 4)
            friend.minerals += int(self.minerals / 4)
            self.energy -= int(self.energy / 4)
            self.minerals -= int(self.minerals / 4)

    def update_commands(self, draw_type):
        pos2 = self.get_rotate_position(self.rotate)
        for x in range(5):
            #[command] абсолютно - выполняет команду в направлении бота
            #[command] относительно - выполняет команду в направлении, указываемом остатком от деления номера следующей команды на 8
            command = self.commands[self.index]
            #--------------------------------------------------------
            if command == 23:#повернуться
                self.rotate += self.commands[(self.index + 1) % 64] % 8
                self.rotate %= 8
                self.next_command(2)
            elif command == 24:#сменить направление
                self.rotate = self.commands[(self.index + 1) % 64] % 8
                self.next_command(2)
            #--------------------------------------------------------
            elif command == 25:#фотосинтез
                sector = self.bot_in_sector()
                if sector <= 5:
                    self.energy += self.photo_list[sector]
                    self.photo_count += 1
                self.next_command()
                break
            #--------------------------------------------------------
            elif command == 26:#походить относительно
                stc = self.rotate
                self.rotate = self.commands[(self.index + 1) % 64] % 8
                sensor = self.sensor(self.world, self.rotate)
                if sensor == 2:
                    self.energy -= 1
                self.move(self.world)
                self.rotate = stc
                self.next_command(2)
                break
            elif command == 27:#походить абсолютно
                sensor = self.sensor(self.world, self.rotate)
                if sensor == 2:
                    self.energy -= 1
                self.move(self.world)
                self.next_command()
                break
            #--------------------------------------------------------
            elif command == 28:#атаковать относительно
                rotate = self.commands[(self.index + 1) % 64] % 8
                pos = self.get_rotate_position(rotate)
                self.attack(pos)
                self.next_command(2)
                break
            elif command == 29:#атаковать абсолютно
                self.attack(pos2)
                self.next_command()
                break
            #--------------------------------------------------------
            elif command == 30:#посмотреть относительно
                rotate = self.commands[(self.index + 1) % 64] % 8
                sensor = self.sensor(self.world, rotate) + 1
                self.next_command(sensor)
            elif command == 31:#посмотреть абсолютно
                sensor = self.sensor(self.world, self.rotate)
                self.next_command(sensor)
            #--------------------------------------------------------
            elif command == 34 or command == 50:#отдать ресурсы относительно
                rotate = self.commands[(self.index + 1) % 64] % 8
                pos = self.get_rotate_position(rotate)
                self.give(pos)
                self.next_command(2)
                break
            elif command == 35 or command == 52:#отдать ресурсы абсолютно
                pos = self.get_rotate_position(self.rotate)
                self.give(pos)
                self.next_command()
                break
            #--------------------------------------------------------
            elif command == 36:#сколько у меня энергии
                cmd = self.commands[(self.index + 1) % 64]
                if self.energy >= cmd * 15:
                    self.next_command(self.commands[(self.index + 2) % 64])
                else:
                    self.next_command(self.commands[(self.index + 3) % 64])
            elif command == 37:#сколько у меня минералов
                cmd = self.commands[(self.index + 1) % 64]
                if self.minerals >= cmd * 15:
                    self.next_command(self.commands[(self.index + 2) % 64])
                else:
                    self.next_command(self.commands[(self.index + 3) % 64])
            #--------------------------------------------------------
            elif command == 38:#преобразовать минералы в энергию
                if self.minerals != 0:
                    self.minerals_count += 1
                self.energy += self.minerals * 4
                self.minerals = 0
                self.next_command()
                break
            #--------------------------------------------------------
            elif command == 39:#есть ли приход энергии
                sector = self.bot_in_sector()
                if sector >= 0 and sector <= 5:
                    self.next_command(self.commands[(self.index + 1) % 64])
                else:
                    self.next_command(self.commands[(self.index + 2) % 64])
            elif command == 40:#есть ли приход минералов
                sector = self.bot_in_sector()
                if sector >= 5 and sector <= 7:
                    self.next_command(self.commands[(self.index + 1) % 64])
                else:
                    self.next_command(self.commands[(self.index + 2) % 64])
            #--------------------------------------------------------
            elif command == 41:#поделиться относительно
                rotate = self.commands[(self.index + 1) % 64] % 8
                self.multiply(draw_type, rotate)
                self.next_command(2)
                break
            elif command == 42:#поделиться абсолютно
                self.multiply(draw_type, self.rotate)
                self.next_command()
                break
            #--------------------------------------------------------
            elif command == 43:#какая моя позиция(x)
                cmd = self.commands[(self.index + 1) % 64] / 64
                if self.pos[0] / self.world_scale[0] >= cmd:
                    self.next_command(self.commands[(self.index + 2) % 64])
                else:
                    self.next_command(self.commands[(self.index + 3) % 64])
            elif command == 44:#какая моя позиция(y)
                cmd = self.commands[(self.index + 1) % 64] / 64
                if self.pos[1] / self.world_scale[1] >= cmd:
                    self.next_command(self.commands[(self.index + 2) % 64])
                else:
                    self.next_command(self.commands[(self.index + 3) % 64])
            #--------------------------------------------------------
            else:#безусловный переход
                self.next_command(command)

    def update(self, draw_type):
        self.bots[0] += 1
        if not self.killed:
            self.world[self.pos[0]][self.pos[1]] = "bot"
            self.age -= 1
            self.energy -= 1
            sector = self.bot_in_sector()
            if sector <= 7 and sector >= 5:
                self.minerals += self.minerals_list[sector - 5]
            if draw_type != self.last_draw_type:
                self.last_draw_type[0] = draw_type
                self.change_image(draw_type)
            self.update_commands(draw_type)
            if self.energy >= 800:
                self.multiply(draw_type, self.rotate)
            if self.energy > 1000:
                self.energy = 1000
            if self.minerals > 1000:
                self.minerals = 1000
            if self.age <= 0:
                self.world[self.pos[0]][self.pos[1]] = "organics"
                self.objects.add(organics.Organics(self.pos, self.world, self.objects, self.energy))
                #self.world[self.pos[0]][self.pos[1]] = "none"
                self.killed = 1
                self.kill()
            if self.energy <= 0:
                self.world[self.pos[0]][self.pos[1]] = "none"
                self.killed = 1
                self.kill()
