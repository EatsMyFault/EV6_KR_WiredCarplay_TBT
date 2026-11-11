# EV6 KOR 유선 CarPlay TBT 설치 가이드

한국 내수형 EV6에 설치된 HMG-API에서 v0.1.6 모드를 추가하는 방법입니다. 같은 작성자 서명으로 설치한 이전 버전도 이 순서로 업데이트할 수 있습니다.

> 차량을 주차한 상태에서 설치하고 처음 동작을 확인하세요. 설치, 적용 또는 사용으로 발생한 오류, 데이터 손실, 차량 장치의 손상 등에 대해 제작자는 책임지지 않습니다.

## 1. 차량과 준비물 확인

| 확인 항목 | 조건 |
| --- | --- |
| 차량과 내비 | 한국 내수형 EV6, 표준형 5W(Gen5W) |
| 내비 소프트웨어 | `CVEV / KOR / 260902` |
| HMG-API | `1.0.1` 이상이 설치되어 있고, 앱 상단에 **Hooks on**이 표시됨 |
| 준비물 | FAT32 또는 exFAT USB 메모리, 순정 유선 CarPlay가 작동하는 iPhone과 데이터 케이블 |

차량 앱 목록에서 **HMG-API**를 열어 앱 버전과 차량 정보, **Hooks on** 표시를 확인합니다. HMG-API 앱 아이콘이 안 보이면 **NEMESIS Manager → My Packages → HMG-API → Launch**에서 열 수 있습니다. 차량 코드나 펌웨어가 다르면 적용 전에 지원 목록을 확인하세요. [HMG-API 차량 지원 안내](https://github.com/Open-HMG/HMG-API/wiki/Supported-cars)

## 2. 설치 ZIP 받기

GitHub의 v0.1.6 릴리스에서 **Assets**를 펼쳐 `EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip`을 받으세요. [v0.1.6 릴리스](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/releases/tag/v0.1.6) · [설치 ZIP 다운로드](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/releases/download/v0.1.6/EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip)

## 3. USB에 복사하기

1. Windows 파일 탐색기에서 USB 드라이브를 우클릭하고 **속성**을 열어 파일 시스템을 확인합니다. FAT32 또는 exFAT이면 바로 사용하면 됩니다.
2. USB를 열었을 때 처음 보이는 위치에 `HMGAPI` 폴더를 만들고, 그 안에 `updates` 폴더를 만듭니다. 폴더가 이미 있으면 그대로 사용하세요.
3. 다운로드한 **ZIP 파일 그대로** `updates` 폴더에 복사합니다. `F:`는 아래 예시의 USB 드라이브 문자입니다.

```text
F:\
└─ HMGAPI
   └─ updates
      └─ EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip
```

USB가 다른 파일 시스템으로 되어 있어 포맷이 필요하면 먼저 파일을 백업하세요. 포맷하면 USB의 기존 파일이 지워집니다.

## 4. 차량에서 설치하고 적용하기

1. 주차 상태에서 내비가 켜진 뒤, 평소 iPhone을 연결해 CarPlay를 쓰는 포트에 USB를 꽂습니다.
2. **HMG-API → Check USB updates**를 열고 **EV6 KOR Wired CarPlay TBT (experimental) v0.1.6**을 선택합니다. 첫 설치에서 작성자 신뢰 확인이 표시되면 다운로드한 출처와 모드 이름을 확인한 뒤 진행합니다.
3. **Mods**에서 모드가 **On**인지 확인하고 **Apply changes**를 누릅니다. 재시작 안내가 나오면 내비를 재시작합니다.
4. 설치를 마치면 USB를 빼고, 같은 데이터 포트에 iPhone을 연결합니다. [HMG-API 모드 설치 및 업데이트 안내](https://github.com/Open-HMG/HMG-API/wiki/Installing-and-removing-mods)

## 5. 길안내 확인하기

CarPlay에서 평소 사용하는 지도 앱을 열고 목적지를 설정합니다. 안내가 시작되면 계기판과 HUD에서 다음 회전 화살표와 거리가 표시되는지 확인합니다. 주행 중 다음 회전으로 넘어가거나 경로가 재탐색되면 새 화살표와 거리가 반영되는지 확인할 수 있습니다. 표시 항목은 지도 앱이 보내는 안내 정보와 차량의 표시 장비에 따라 달라집니다.

## 설치나 표시가 잘 안 될 때

| 증상 | 확인할 내용 |
| --- | --- |
| **Check USB updates**에 모드가 보이지 않음 | USB의 `HMGAPI/updates/` 안에 `EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip` 파일이 그대로 있는지, USB가 데이터 포트에 꽂혀 있는지 확인합니다. |
| 모드는 보이지만 적용되지 않음 | **Hooks on**, `CVEV / KOR / 260902`, **Mods**의 활성 상태, **Apply changes**에 표시된 오류 또는 재시작 안내를 확인합니다. |
| CarPlay 길안내는 되지만 계기판이나 HUD가 갱신되지 않음 | 정차 후 **HMG-API → Logs**에서 `EV6_TBT_016`을 찾습니다. `RG_RECEIVED`는 안내 메시지 수신, `RG_STEP`은 회전 번호와 모드가 선택한 화살표 및 거리를 보여줍니다. |

증상을 제보할 때는 차량 펌웨어, HMG-API와 TBT 모드 버전, 지도 앱 이름, 문제가 난 상황과 관련 로그를 함께 적어 주세요. 공개 이슈에 올리기 전에 VIN, 기기 식별 정보, 목적지 등 개인 정보를 가려 주세요. [문제 제보 양식](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/issues/new?template=bug-report.yml)

## 끄거나 삭제하기

**HMG-API → Mods**에서 **EV6 KOR Wired CarPlay TBT (experimental)**을 **Off**로 바꾸면 잠시 끌 수 있습니다. 모드를 삭제할 때는 **Remove**를 선택하세요. 두 경우 모두 **Apply changes**를 누르고 재시작 안내가 나오면 따릅니다. [HMG-API 모드 제거 안내](https://github.com/Open-HMG/HMG-API/wiki/Installing-and-removing-mods#removing)

[프로젝트 README](../README.md) · [버전별 변경 내역](../CHANGELOG.md) · [문서 구성 참고](https://github.com/StopDragon/korea-projection/blob/main/docs/install-guide.md)
