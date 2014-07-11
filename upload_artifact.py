from os import environ as env
from glob import glob

import datetime
import dropbox

def main():
    client = dropbox.client.DropboxClient(env.get('DROPBOX_ACCESS_TOKEN'))
    filename = glob.glob('target/InventoryBomb-*.jar')[0]
    base = filename.split('.jar')[0]
    date = datetime.datetime.now().strftime("%Y-%m-%d")
    build_id = env['TRAVIS_JOB_NUMBER']

    with open(filename) as f:
        client.put_file('/' + base + date + '-' + build_id, f, overwrite=True)
    print("Successfully uploaded latest build to Dropbox" )
