#!/bin/sh


./gradlew clean build -x test


eb deploy
