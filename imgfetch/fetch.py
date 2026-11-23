import json,os,sys,time,urllib.parse,urllib.request,io
from PIL import Image,ImageDraw,ImageFont
UA={"User-Agent":"bm-catalog-image-fetch/1.0"}
def get(u,t=30):
    for i in range(3):
        try:
            return urllib.request.urlopen(urllib.request.Request(u,headers=UA),timeout=t).read()
        except Exception as e:
            err=e;time.sleep(2)
    raise err
os.makedirs("out/cand",exist_ok=True)
meta={}
for line in open("queries.tsv",encoding="utf-8"):
    pid,off,com=line.rstrip("\n").split("\t");cands=[]
    if off:
        try:
            q=urllib.parse.urlencode({"search_terms":off,"json":1,"page_size":12,"fields":"code,product_name,brands,image_front_url,countries_tags"})
            d=json.loads(get("https://world.openfoodfacts.org/cgi/search.pl?"+q))
            ps=[p for p in d.get("products",[]) if p.get("image_front_url")]
            ps.sort(key=lambda p:0 if "en:egypt" in (p.get("countries_tags") or []) else 1)
            for p in ps[:4]:
                cands.append({"src":"off","url":p["image_front_url"],"title":(p.get("brands","")+" | "+p.get("product_name",""))[:60],"lic":"CC BY-SA 3.0 Open Food Facts","page":"https://world.openfoodfacts.org/product/"+p["code"]})
        except Exception as e: print("off",pid,e)
        time.sleep(1)
    try:
        q=urllib.parse.urlencode({"action":"query","generator":"search","gsrsearch":"filetype:bitmap "+com,"gsrnamespace":6,"gsrlimit":4,"prop":"imageinfo","iiprop":"url|extmetadata","iiurlwidth":400,"format":"json"})
        d=json.loads(get("https://commons.wikimedia.org/w/api.php?"+q))
        for pg in sorted((d.get("query",{}).get("pages",{}) or {}).values(),key=lambda x:x.get("index",0)):
            ii=pg["imageinfo"][0];em=ii.get("extmetadata",{})
            cands.append({"src":"commons","url":ii["thumburl"],"title":pg["title"][5:65],"lic":em.get("LicenseShortName",{}).get("value",""),"artist":em.get("Artist",{}).get("value","")[:200],"page":ii["descriptionurl"]})
    except Exception as e: print("com",pid,e)
    for i,c in enumerate(cands):
        try:
            im=Image.open(io.BytesIO(get(c["url"]))).convert("RGB");im.thumbnail((400,400));im.save(f"out/cand/{pid}_{i}.jpg",quality=85);c["file"]=f"{pid}_{i}.jpg"
        except Exception as e: print("dl",pid,i,e)
    meta[pid]=[c for c in cands if "file" in c]
    print(pid,len(meta[pid]),flush=True)
json.dump(meta,open("out/meta.json","w"),ensure_ascii=False,indent=1)
# contact sheets: 10 items per sheet, up to 8 candidates per row
ids=list(meta);W=150
try: font=ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",14)
except: font=ImageFont.load_default()
for s in range(0,len(ids),10):
    chunk=ids[s:s+10];sheet=Image.new("RGB",(80+8*W,len(chunk)*W),"white");dr=ImageDraw.Draw(sheet)
    for r,pid in enumerate(chunk):
        dr.text((5,r*W+60),pid,fill="black",font=font)
        for i,c in enumerate(meta[pid][:8]):
            im=Image.open("out/cand/"+c["file"]);im.thumbnail((W-6,W-20));sheet.paste(im,(80+i*W,r*W));dr.text((80+i*W,r*W+W-18),str(i),fill="red",font=font)
    sheet.save(f"out/sheet_{s//10:02d}.jpg",quality=80)
