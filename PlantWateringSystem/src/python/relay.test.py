import RPi.GPIO as GPIO
import time

channel = 17

# GPIO setup
GPIO.setmode(GPIO.BCM)
GPIO.setup(channel, GPIO.OUT) # Assign the pin here


def relay_on(pin):
	GPIO.output(pin, GPIO.HIGH)  # Turn relay on


def relay_off(pin):
	GPIO.output(pin, GPIO.LOW)  # Turn relay off


if __name__ == '__main__':
	try:
		relay_on(channel)
		time.sleep(1)
		relay_off(channel)
		time.sleep(1)
		GPIO.cleanup()
	except KeyboardInterrupt:
		GPIO.cleanup()
