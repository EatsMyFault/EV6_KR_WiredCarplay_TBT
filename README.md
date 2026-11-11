# EV6 KOR 유선 CarPlay TBT

한국 내수형 EV6의 순정 **유선 Apple CarPlay**에서 지도 앱이 보내는 회전 방향, 거리, 차선 안내를 계기판과 HUD로 전달하는 HMG-API 모드입니다. TBT는 “300m 앞 우회전”처럼 다음 회전을 알려주는 안내를 뜻합니다. 표시되는 항목은 지도 앱이 보내는 정보와 차량의 표시 장비에 따라 달라집니다.

**최신 정식 릴리스 v0.1.6** · [설치 ZIP 다운로드](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/releases/download/v0.1.6/EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip)
· [v0.1.6 릴리스](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/releases/tag/v0.1.6)
· [처음 설치하는 분을 위한 가이드](docs/install-guide.md)
· [버전별 변경 내역](CHANGELOG.md)

> **실차 확인** · 한국 내수형 EV6 표준형 5W(`CVEV / KOR / 260902`)의 유선 CarPlay에서 회전 안내 전환과 경로 재탐색 후 안내가 정상 작동했습니다. 설치와 첫 확인은 주차 상태에서 진행하세요.
>
> **주의사항** · 설치와 사용 여부는 사용자가 직접 판단해 주세요. 이 모드의 설치, 적용 또는 사용으로 발생한 오류, 데이터 손실, 차량 장치의 손상 등에 대해 제작자는 책임지지 않습니다.

## 설치 전 확인

| 항목 | 요구 사항 |
| --- | --- |
| 차량 | 한국 내수형 EV6, 표준형 5W(Gen5W) |
| 대상 펌웨어 | `CVEV / KOR / 260902` |
| HMG-API | `1.0.1` 이상. NEMESIS 사용자는 시작 지연을 수정한 `1.0.2` 권장 |
| 후크 | 해당 차량용 후크 맵이 적용되어 HMG-API에 **Hooks on** 표시 |
| 연결 | 순정 유선 Apple CarPlay가 동작하는 iPhone, 데이터 USB 포트와 케이블 |

HMG-API가 설치되어 있으면 앱을 열어 버전과 **Hooks on** 상태를 확인하세요. 앱 아이콘이 보이지 않으면 **NEMESIS Manager → My Packages → HMG-API → Launch**로 열 수 있습니다. **Hooks off**이거나 차량·펌웨어가 다르면 적용 전에 [HMG-API 차량 지원 안내](https://github.com/VysseCodes/HMG-API/wiki/Supported-cars)를 확인하세요.

## EV6 TBT 설치 및 적용

### 설치 USB 경로

FAT32 또는 exFAT USB의 최상위에 `HMGAPI` 폴더를 만들고, 그 안에 `updates` 폴더를 만드세요. 설치 ZIP을 **압축 해제하지 않고** 아래 위치에 복사합니다.

```text
F:\HMGAPI\updates\EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip
```

`F:`는 Windows 예시입니다. 탐색기에서 USB에 다른 드라이브 문자가 붙어 있다면 그 문자를 사용하세요.

### 차량에서 설치

1. 위 다운로드 링크 또는 릴리스의 **Assets**에서 `EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip`을 받습니다.
2. 다운로드한 설치 ZIP을 위의 USB 경로에 복사합니다.
3. 주차 상태에서 USB를 차량의 데이터 포트에 꽂고, **HMG-API 앱 → Check USB updates**를 누릅니다.
4. 목록에서 **EV6 KOR Wired CarPlay TBT (experimental)** v0.1.6을 선택합니다. 첫 설치에서는 작성자 신뢰 확인이 표시될 수 있습니다.
5. **Mods**에서 모드가 켜져 있는지 확인하고 **Apply changes**를 누릅니다. 계획에 오류가 표시되면 원인을 확인하고, 재시작 안내가 나오면 따릅니다.
6. 설치 USB를 뺀 뒤 같은 데이터 포트에 iPhone을 케이블로 연결합니다. CarPlay에서 목적지를 설정해 경로 안내를 시작하고, 계기판과 HUD에 각각 무엇이 표시되는지 확인합니다.

기존 EV6 TBT 모드와 같은 ID 및 서명자를 사용하므로 동일한 서명으로 설치된 이전 버전은 위 순서로 업데이트할 수 있습니다. [HMG-API 모드 설치 및 업데이트 안내](https://github.com/VysseCodes/HMG-API/wiki/Installing-and-removing-mods)

## 확인과 문제 해결

| 증상 | 확인할 것 |
| --- | --- |
| **Check USB updates**에 모드가 안 보임 | USB의 `HMGAPI/updates/` 아래에 설치용 ZIP이 그대로 있는지 확인합니다. |
| 모드가 로드되지 않음 | `CVEV / KOR / 260902`, **Hooks on**, 적용 계획의 오류 메시지를 확인합니다. |
| CarPlay는 열리지만 안내가 안 보이거나 이전 안내가 남음 | **Mods → 모드 로그** 또는 **Logs**에서 `EV6_TBT_016`을 찾습니다. `REQUEST_START accepted=true`는 요청 수락, `RG_RECEIVED`는 데이터 수신을 뜻합니다. `RG_STEP`에는 현재 회전 번호, 선택한 아이콘, 모드가 받은 거리와 표시용 거리가 기록됩니다. |
| 사용을 중단하거나 원래 동작으로 돌아가고 싶음 | HMG-API **Mods**에서 EV6 TBT 모드를 **Off** 또는 **Remove**로 바꾸고 **Apply changes**를 누릅니다. 재시작 안내가 나오면 따릅니다. 앱을 열 수 없다면 [HMG-API 문제 해결 안내](https://github.com/VysseCodes/HMG-API/wiki/Troubleshooting)를 확인합니다. |

설치 오류나 길안내 문제는 GitHub 양식에 차량 펌웨어, 모드 버전, 증상을 적어 주세요. 공개 이슈에 첨부하는 로그나 사진에서는 VIN과 위치 등 개인 정보를 가려 주세요. [문제 제보 양식](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/issues/new?template=bug-report.yml)

## v0.1.6에서 바뀐 점

- 회전 각도가 생략돼도 종류가 명확한 좌·우회전, 직진, 유턴, 좌·우 방향 유지를 표시합니다. 회전교차로는 받은 각도에 맞춰 출구 방향을 표시합니다.
- 새 안내의 방향을 표시할 수 없으면 이전 화살표와 회전 거리를 지웁니다. 거리만 갱신하는 메시지는 기존 회전 각도를 유지합니다.
- 재탐색 뒤 새 회전 상세가 도착하면 바로 표시합니다. `RG_STEP` 로그에 회전 번호와 종류, 선택한 아이콘, 거리를 남깁니다.

이전 버전의 변경 내역은 [CHANGELOG.md](CHANGELOG.md)에 있습니다.

## 소스 및 빌드

모드 소스는 [mod/](mod/)에 있습니다. [BUILD_0.1.6.sh](BUILD_0.1.6.sh)는 Bash/Ubuntu WSL에서 JDK, Python 3, Android SDK의 `android-29`와 `build-tools/35.0.0`, HMG-API v1.0.1의 `out/classes` 및 `out/engine-host`를 사용합니다. `HMGAPI_BUILD`로 HMG-API 빌드 폴더를, `EV6_SIGNING_KEY`로 기존 작성자 키 경로를 지정할 수 있습니다. 배포본의 업데이트 서명을 생성하려면 기존 작성자 키가 필요합니다.

라이선스는 [AGPL-3.0](LICENSE)입니다. HMG-API, 아이오닉 5 프로젝트, Korea Projection의 출처는 [NOTICE.txt](NOTICE.txt)에 정리했습니다.
