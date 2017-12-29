#!/bin/bash

now rm $(now ls|grep "kukubot-"|cut -d" " -f2)
