import pygame
pygame.init()

class GameObject(pygame.sprite.Sprite):
    movelist = [
        (0, -1),
        (1, -1),
        (1, 0),
        (1, 1),
        (0, 1),
        (-1, 1),
        (-1, 0),
        (-1, -1),
        ]
    def __init__(self, pos, image):
        pygame.sprite.Sprite.__init__(self)
        self.name = None
        self.rotate = 0
        self.image = image
        self.pos = pos
        self.rect = self.image.get_rect()
        self.rect.x = self.pos[0] * 10
        self.rect.y = self.pos[1] * 10
        self.W = pygame.display.Info().current_w
        self.H = pygame.display.Info().current_h
        self.border = int(self.H / 10)
        self.world_scale = [
            int((self.W - 300) / 10),
            int(self.H / 10)
            ]
        self.world_scale = [60, 60]

    def update(self):
        pass

    def move(self, world):
        pos2 = [
            (self.pos[0] + GameObject.movelist[self.rotate][0]) % self.world_scale[0],
            self.pos[1] + GameObject.movelist[self.rotate][1]
            ]
        if pos2[1] >= 0 and pos2[1] <= self.world_scale[1] - 1:
            if world[pos2[0]][pos2[1]] == "none":
                world[self.pos[0]][self.pos[1]] = "none"
                world[pos2[0]][pos2[1]] = self.name
                self.pos[0] += GameObject.movelist[self.rotate][0]
                self.pos[0] %= self.world_scale[0]
                self.pos[1] += GameObject.movelist[self.rotate][1]
                self.rect.x = self.pos[0] * 10
                self.rect.y = self.pos[1] * 10

    def sensor(self, world, rotate):#1 - border, 2 - none, 3 - bot, 4 - relative, 5 - organics
        pos2 = [
            (self.pos[0] + GameObject.movelist[rotate][0]) % self.world_scale[0],
            self.pos[1] + GameObject.movelist[rotate][1]
            ]
        if pos2[1] >= 0 and pos2[1] <= self.world_scale[1] - 1:
            if world[pos2[0]][pos2[1]] == "bot":
                for select_bot in self.objects:
                    if select_bot.pos == pos2 and select_bot.killed == 0:
                        break
                    else:
                        select_bot = None
                if select_bot != None:
                    if self.name != "organics":
                        try:
                            if select_bot.commands == self.commands:
                                return(4)
                            else:
                                return(3)
                        except:
                            return(5)
                    else:
                        return(3)
            elif world[pos2[0]][pos2[1]] == "organics":
                return(5)
            elif world[pos2[0]][pos2[1]] == "none":
                return(2)
        else:
            return(1)
