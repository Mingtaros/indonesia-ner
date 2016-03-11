# Named Entity Recoginition for Bahasa Indonesia

API untuk kode ini dapat dilihat pada [http://yusufsyaifudin.github.io/indonesia-ner/doc/api](http://yusufsyaifudin.github.io/indonesia-ner/doc/api).

## Instalasi

Untuk menginstall, tambahkan kode berikut pada berkas `pom.xml`:

```
<repositories>
    <repository>
      <id>yusufs.nlp</id>
      <name>indonesia-ner</name>
      <url>https://github.com/yusufsyaifudin/indonesia-ner/raw/1.0.0/</url>
    </repository>
</repositories> 
```

dan kode berikut pada _dependency_ `pom.xml`

```
<dependencies>
    <dependency>
      <groupId>yusufs.nlp</groupId>
      <artifactId>indonesia-ner</artifactId>
      <version>1.0.0</version>
      <scope>compile</scope>
    </dependency>
</dependencies>
```

Kemudian, pastikan Anda telah menyalin dan menaruh folder `resources` ke dalam *root directory* project Anda, sehingga akan terbentuk struktur sebagai berikut:

```
- your-project
  - resources <-- tempat Anda menaruh file resource yang ada dalam repositori ini
  - src
    - main
      - java
  - target
```


# Melakukan prediksi
Anda bisa juga langsung melakukan prediksi dengan menggunakan model yang telah saya buat, yaitu:

```
IndonesiaNER iner = new IndonesiaNER(IndonesiaNER.MODEL.YUSUFS);
ArrayList<Sentence> predicted = iner.predictWithEmbeddedModel("Jokowi pergi ke Singapura.", true);


```

Anda akan mendapatkan keluaran dengan struktur berikut:

```
[
  {
    originalSentence:Jokowi pergi ke Singapura.
  },
  {
    tokenizedSentence:Jokowi pergi ke Singapura .
  },
  word:[
    {
      token:Jokowi,
      postag:NNPFC,
      tag:PERSON
    },
    {
      token:pergi,
      postag:VBI,
      tag:OTHER
    },
    {
      token:ke,
      postag:IN,
      tag:OTHER
    },
    {
      token:Singapura,
      postag:NNFC,
      tag:LOCATION
    },
    {
      token:.,
      postag:.,
      tag:OTHER
    }
  ]
]
```

yang kemudian dapat diiterasi dengan cara berikut:

```
for(ArrayList<Words> arrWords : predicted) {
  for(Words word : arrWords.getWords()) {
    String kata     = word.getToken();  // kata yang diprediksi
    String labelNer = word.getXmlTag(); // hasil prediksi label entitas untuk kata tersebut
    String labelPos = word.getPosTag(); // postag dari kata tersebut
  }
}
```


# Membuat Model dan Prediksi dari model buatan sendiri
## Membuat model dari data latih

```
Train train = new Train();
NERModel model;

try {
    model = train.doTrain(String trainingData, Boolean withPunctuation); // akan melakukan pemecahan kalimat secara otomatis di dalamnya
    model = doTrainReadLine(File trainingData, Boolean withPunctuation); // menganggap bahwa satu baris merupakan satu kalimat

} catch (Exception e) {
    throw new Exception(e.getMessage());
}
 
```

## Melakukan prediksi dari model yang dibuat
```
Prediction pred = new Prediction();

ArrayList<Sentence> sentence = pred.predict(String data, Boolean withPunctuation, NERModel nermodel);

```

# Melakukan Perhitungan Probabilitas
Anda mungkin hanya butuh melakukan perhitungan probabilitas, Anda dapat melakukannya dengan menggunakan kelas `Counter` yang dimasukkan sebuah `ArrayList<Sentence>` caranya ialah sebagai berikut:


Pertama, lakukan pemecahan kalimat dari teks yang ada (lihat [https://github.com/yusufsyaifudin/tokenizer-id](https://github.com/yusufsyaifudin/tokenizer-id)). Kemudian, untuk setiap kalimat yang ditemukan dalam pemecahan, diiterasi dan ditambahkan ke dalam array.

Array tersebut kemudian dapat dihitung menggunakan kelas `Counter` yang didalamnya dapat dilihat probabilitas awal, probabilitas transisi, dan probabilitas bersyarat yang meliputi:

* probabilitas antara kelas kata sebelumnya dengan label entitas kata tersebut
* probabilitas antara kelas kata tersebut dengan label entitas kata tersebut
* probabilitas antara kelas kata sesudahnya dengan label entitas kata tersebut

berikut kodenya:

```
Tokenizer tokenizer = new Tokenizer();
ArrayList<String> sentences = tokenizer.extractSentence(trainingData);

ArrayList<Sentence> sentencesArray = new ArrayList<>();
TextSequence ws = new TextSequence();
for(String sentence : sentences) {
  Sentence sen;
  try {
    // Akan menghasilkan kumpulan token + ner tag + pos tag nya.
    sen = ws.wordSeqWithTag(sentence, withPunctuation);
    // Tambahkan setiap hasil training ke variable wordSeq.
    sentencesArray.add(sen);
  } catch (Exception e) {
    logger.error("Gagal dalam melakukan training data.");
    e.printStackTrace();
    throw new Exception("Gagal dalam melakukan training data.");
  }
}

Counter counter = new Counter(sentencesArray);
HashMap<String, Double> startProbability = counter.probabilityOfStartXmlTag;
HashMap<String, HashMap<String, Double>> transitionProbability = counter.probabilityBetweenXmlTag;
    
// Emission probability menggunakan probabilitas tag xml dengan melihat kelas katanya, kelas kata sebelumnya dan kelas kata berikutnya.
HashMap<String, HashMap<String, Double>> previousLexicalClass = counter.probabilityOfCurrentXmlAndPrevLex;
HashMap<String, HashMap<String, Double>> currentLexicalClass = counter.probabilityOfCurrentXmlAndCurrentLex;
HashMap<String, HashMap<String, Double>> nextLexicalClass = counter.probabilityOfCurrentXmlAndNextLex;

```


# Citation
You must add this to your citation or bibliography:

```
@mastersthesis{ Syaifudin2016,
       author       = "Yusuf Syaifudin",
       title        = "QUOTATIONS IDENTIFICATION FROM INDONESIAN ONLINE NEWS USING RULE-BASED METHOD",
       school       = {Universitas Gadjah Mada},
       note         = {Undergraduate Thesis}
       year         = "2016" }

@mastersthesis{ Fachri2014,
       author       = "Muhammad Fachri",
       title        = "NAMED ENTITY RECOGNITION FOR INDONESIAN TEXT USING HIDDEN MARKOV MODEL",
       school       = {Universitas Gadjah Mada},
       note         = {Undergraduate Thesis}
       year         = "2014" }
```

# LICENSE
The MIT License (MIT)
Copyright (c) <2016> <Yusuf yusufs.syaifudin@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

<!-- mvn install:install-file -DgroupId=yusufs.nlp -DartifactId=indonesia-ner -Dversion=1.0.0 -Dpackaging=jar -Dfile="D:\Project\nerID\target\indonesia-ner-1.0.0.jar" -DlocalRepositoryPath="D:\Project\nerID" -->
