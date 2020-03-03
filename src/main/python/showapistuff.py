import requests

s = requests.Session()
s.headers = {
    'user-agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.90 Safari/537.36',
}
sub_json = s.get("http://arcane-fortress-22748.herokuapp.com/api/user/rall.json").json()

print(sub_json)
