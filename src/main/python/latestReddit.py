import requests
url = 'https://old.reddit.com/r/programming/new.json?limit=20'
s = requests.Session()
headers = {
    'authority': 'old.reddit.com',
    'pragma': 'no-cache',
    'cache-control': 'no-cache',
    'upgrade-insecure-requests': '1',
    'user-agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.90 Safari/537.36',
    'accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3',
    'accept-encoding': 'gzip, deflate, br',
    'accept-language': 'en-US,en;q=0.9',
    'dnt': '1',
}
webpage = s.get(url, headers=headers).json()
tmp = []
for key, val in webpage.items():
    if key == "data":
        for child in val['children']:
            data = child.get('data')
            for pair in data.items():
                if pair[0] == 'stickied' and pair[1] is False:
                    tmp.append(
                        {
                            'title': data['title'],
                            'upvotes': data['ups'],
                            'url': data['permalink']
                        }
                    )
final = tmp
print(final)
