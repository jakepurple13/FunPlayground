import requests
import json
import sys

s = requests.Session()
s.headers = {
    'user-agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.90 Safari/537.36',
}
sub_name = sys.argv[1]  # 'programming'
sub_level = sys.argv[2]
sub_json = s.get(f'https://old.reddit.com/r/{sub_name}/{sub_level}.json?limit=20').json()['data']['children']

res = map(lambda x: x.get('data'), filter(lambda x: x['data'].get('stickied') is False, sub_json))
entries = [{
    'title': r.get('title'),
    'upvotes': r.get('ups'),
    'url': r.get('permalink')
} for r in res]
print(json.dumps(entries, indent=4))
