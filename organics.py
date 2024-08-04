from game_object import GameObject
import image_factory
import pygame
pygame.init()

class Organics(GameObject):
    def __init__(self, pos, world, objects, energy=0):
        GameObject.__init__(self, pos, image_factory.get_organics_image())
        self.name = "organics"
        self.rotate = 4
        self.energy = energy
        self.world = world
        self.objects = objects
        self.killed = 0
        self.is_falling = 1

    def update(self, a):
        if not self.killed:
            self.world[self.pos[0]][self.pos[1]] = "organics"
            if self.is_falling:
                self.move(self.world)
                sensor = self.sensor(self.world, self.rotate)
                if sensor != 2:
                    self.is_falling = 0
