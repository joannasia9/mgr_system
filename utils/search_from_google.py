from selenium import webdriver
import json
import os
from urllib.request import urlopen, Request
import argparse


ap = argparse.ArgumentParser()
ap.add_argument("-q", "--query", required=True, help="search term")
args = vars(ap.parse_args())


searchterm = args["query"]
url = "https://www.google.co.in/search?q=" + searchterm + "&source=lnms&tbm=isch"
browser = webdriver.Safari()
browser.get(url)

header = {"Safari/12.1.1 (macOS Mojave) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36"}
counter = 0
succounter = 0

if not os.path.exists(searchterm):
    os.mkdir(searchterm)

for _ in range(200):
    browser.execute_script("window.scrollBy(0,10000)")

for x in browser.find_elements_by_xpath("//div[@class='rg_meta']"):
    counter = counter + 1
    print ("Total Count: %s" % counter)
    print ("Succsessful Count: %s" % succounter)
    print ("URL: %s" % {json.loads(x.get_attribute('innerHTML'))["ou"]})

    img = json.loads(x.get_attribute('innerHTML'))["ou"]
    imgtype = json.loads(x.get_attribute('innerHTML'))["ity"]
    try:
        req = Request(img, headers={'User-Agent': header})
        raw_img = urlopen(req).read()
        File = open(os.path.join(searchterm, searchterm + "_" + str(counter) + "." + imgtype), "wb")
        File.write(raw_img)
        File.close()
        succounter = succounter + 1
    except:
        print("can't get img")

print ("pictures succesfully downloaded")
browser.close()
