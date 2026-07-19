# 에이전트 핸드오프: 나라별 노인용 공개 재생목록 만들기

아래 프롬프트를 브라우저를 조작할 수 있는 에이전트에게 그대로 전달하세요.
전제: 브라우저에 재생목록을 소유할 구글 계정이 YouTube에 로그인되어 있어야 합니다.

---

## 프롬프트 (여기서부터 복사)

당신의 임무는 노인용 TV 앱 "SeniorConnect"가 사용할 **공개 YouTube 재생목록 4개**를
내 로그인된 YouTube 계정에 만드는 것입니다. 앱은 이 재생목록을 임베드 플레이어로
셔플 재생하므로, 재생목록만 잘 만들면 앱 업데이트 없이 콘텐츠가 관리됩니다.

### 만들 재생목록 (나라별 1개)

| 이름 | 언어 | 카테고리 구성 (합계 30~50개) |
|---|---|---|
| `SeniorConnect KR` | 한국어 | 트로트 메들리 10+, 7080 옛날가요 10+, 자연·여행 4K 힐링 5+, 시니어 스트레칭·체조 5+ |
| `SeniorConnect US` | English | Oldies 50s-70s hits 10+, relaxing nature 4K 10+, gentle chair exercise for seniors 5+, classic gospel/hymns 5+ |
| `SeniorConnect IN` | Hindi | Old Hindi songs (golden era) 10+, bhajan/devotional 10+, nature relaxation 5+, gentle yoga for seniors 5+ |
| `SeniorConnect PK` | Urdu | Old Pakistani songs 10+, naat sharif 10+, nature relaxation 5+, light exercise 5+ |

### 영상 선정 기준 (모두 만족해야 함)

1. **길이 10분 이상** 선호 (TV처럼 오래 틀어두는 용도, 짧은 클립 지양)
2. 조회수 높고(대략 10만 이상) 업로드가 오래되어 안정적인 영상 우선
3. 자극적 썸네일, 낚시성 제목, 정치·시사, 쇼츠, 라이브 방송 제외
4. 노인이 듣기 편한 음량·톤 (리믹스/클럽버전 제외)
5. **임베드 허용 확인**: 각 영상을 재생목록에 넣기 전
   `https://www.youtube.com/embed/<VIDEO_ID>` 를 열어 재생되는지 확인.
   "동영상을 재생할 수 없음"이 뜨면 그 영상은 제외
6. 한 채널에 몰아주지 말 것 (채널당 최대 5개)

### 절차

1. YouTube에서 카테고리별로 검색해 기준에 맞는 영상을 고른다
2. 재생목록 4개를 만들고 이름을 위 표대로 지정한다
3. 각 재생목록의 공개 설정을 반드시 **공개(Public)** 로 한다
   (비공개/일부공개는 앱에서 재생 불가)
4. 완료 후 각 재생목록 URL에서 `list=` 뒤의 ID(`PL...`)를 추출한다

### 결과 보고 형식 (이 JSON만 출력)

```json
{
  "KR": "PLxxxxxxxxxxxxxxxx",
  "US": "PLxxxxxxxxxxxxxxxx",
  "IN": "PLxxxxxxxxxxxxxxxx",
  "PK": "PLxxxxxxxxxxxxxxxx"
}
```

## 프롬프트 끝 (여기까지 복사)

---

## 받은 ID를 앱에 넣는 법

`app/src/main/assets/playlists/playlists.json`에서 각 나라의 `"playlist_id": ""`에
받은 `PL...` 값을 넣고 빌드하면 끝. 이후 영상 관리는 YouTube에서 재생목록만
편집하면 되고 앱 재배포는 필요 없다.
