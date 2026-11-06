# EV6 KOR 유선 CarPlay TBT

한국 내수형 EV6의 순정 **유선 Apple CarPlay**에서 지도 앱이 보내는 회전 방향, 거리, 차선 안내를 계기판과 HUD로 전달하는 HMG-API 모드입니다. TBT는 “300m 앞 우회전”처럼 다음 회전을 알려주는 안내를 뜻합니다. 표시되는 항목은 지도 앱이 보내는 정보와 차량의 표시 장비에 따라 달라집니다.

**현재 테스트 버전 v0.1.4** · [설치 ZIP 다운로드](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/releases/download/v0.1.4/EV6_KR_Wired_CarPlay_TBT_v0.1.4_EXPERIMENTAL.zip)
· [v0.1.4 릴리스](https://github.com/EatsMyFault/EV6_KR_WiredCarplay_TBT/releases/tag/v0.1.4)
· [버전별 변경 내역](CHANGELOG.md)

> **실험 버전** · PC에서 빌드, 서명 및 모의 동작을 검증했습니다. 국내 EV6에서 계기판·HUD에 실제로 표시되는지는 아직 검증하지 않았습니다. 설치와 첫 확인은 주차 상태에서 진행하세요.
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
F:\HMGAPI\updates\EV6_KR_Wired_CarPlay_TBT_v0.1.4_EXPERIMENTAL.zip
```

`F:`는 Windows 예시입니다. 탐색기에서 USB에 다른 드라이브 문자가 붙어 있다면 그 문자를 사용하세요.

### 차량에서 설치

1. 위 다운로드 링크 또는 릴리스의 **Assets**에서 `EV6_KR_Wired_CarPlay_TBT_v0.1.4_EXPERIMENTAL.zip`을 받습니다. GitHub가 자동 생성한 **Source code (zip)**은 소스 묶음입니다.
2. 다운로드한 설치 ZIP을 위의 USB 경로에 복사합니다.
3. 주차 상태에서 USB를 차량의 데이터 포트에 꽂고, **HMG-API 앱 → Check USB updates**를 누릅니다.
4. 목록에서 **EV6 KOR Wired CarPlay TBT (experimental)** v0.1.4를 선택합니다. 첫 설치에서는 작성자 신뢰 확인이 표시될 수 있습니다.
5. **Mods**에서 모드가 켜져 있는지 확인하고 **Apply changes**를 누릅니다. 계획에 오류가 표시되면 원인을 확인하고, 재시작 안내가 나오면 따릅니다.
6. 설치 USB를 뺀 뒤 같은 데이터 포트에 iPhone을 케이블로 연결합니다. CarPlay에서 목적지를 설정해 경로 안내를 시작하고, 계기판과 HUD에 각각 무엇이 표시되는지 확인합니다.

기존 EV6 TBT 모드와 같은 ID 및 서명자를 사용하므로 동일한 서명으로 설치된 이전 버전은 위 순서로 업데이트할 수 있습니다. [HMG-API 모드 설치 및 업데이트 안내](https://github.com/VysseCodes/HMG-API/wiki/Installing-and-removing-mods)

## 확인과 문제 해결

| 증상 | 확인할 것 |
| --- | --- |
| **Check USB updates**에 모드가 안 보임 | USB의 `HMGAPI/updates/` 아래에 설치용 ZIP이 그대로 있는지 확인합니다. |
| 모드가 로드되지 않음 | `CVEV / KOR / 260902`, **Hooks on**, 적용 계획의 오류 메시지를 확인합니다. |
| CarPlay는 열리지만 안내가 안 보임 | 실제 경로 안내를 시작하고 **Mods → 모드 로그** 또는 **Logs**에서 `EV6_TBT_014`를 찾습니다. `GUIDANCE_RECOVERY`는 안내 복구를 시도했다는 뜻입니다. `REQUEST_START accepted=true`는 요청 수락, `RG_RECEIVED`는 안내 데이터 수신을 뜻합니다. `RG_PARSED` 이후 실제 출력은 계기판과 HUD에서 확인합니다. |
| 사용을 중단하거나 원래 동작으로 돌아가고 싶음 | HMG-API **Mods**에서 EV6 TBT 모드를 **Off** 또는 **Remove**로 바꾸고 **Apply changes**를 누릅니다. 재시작 안내가 나오면 따릅니다. 앱을 열 수 없다면 [HMG-API 문제 해결 안내](https://github.com/VysseCodes/HMG-API/wiki/Troubleshooting)를 확인합니다. |

동작 결과를 남길 때는 전체 펌웨어 버전, HMG-API 버전, 지도 앱 이름, 계기판/HUD 각각의 결과와 관련 로그를 함께 적어 주세요. 공개 이슈에 VIN이나 기기 식별 정보는 포함하지 마세요.

## v0.1.4에서 바뀐 점

- 지도 앱을 빠르게 바꿀 때 안내 재시작을 놓쳐도, 같은 USB 연결에서 CarPlay 안내가 확인되면 구독을 한 번 다시 요청합니다.
- 일부 일반 좌·우회전 각도를 보완하고, 표시용 거리가 유효하지 않으면 원본 숫자 거리를 사용합니다.
- CarPlay 안내 중 순정 내비의 차선 덮어쓰기를 막고, 안내가 끝나면 모드가 표시한 계기판과 HUD 정보를 정리합니다.

국내 EV6 Car Report에서 17개 후크가 매핑된 것을 확인했습니다. PC에서 안내 복구, 각도·거리, 종료 시 화면 정리, 차선 후크의 모의 테스트와 기존 작성자 서명 검증을 통과했습니다. 자세한 변경 내역은 [CHANGELOG.md](CHANGELOG.md)에 있습니다.

## 소스 및 빌드

모드 소스는 [mod/](mod/)에 있습니다. [BUILD_0.1.4.sh](BUILD_0.1.4.sh)는 Bash/Ubuntu WSL에서 JDK, Python 3, Android SDK의 `android-29`와 `build-tools/35.0.0`, HMG-API v1.0.1의 `out/classes` 및 `out/engine-host`를 사용합니다. `HMGAPI_BUILD`로 HMG-API 빌드 폴더를, `EV6_SIGNING_KEY`로 기존 작성자 키 경로를 지정할 수 있습니다. 배포본의 업데이트 서명을 생성하려면 기존 작성자 키가 필요합니다. 설치에는 키가 필요하지 않습니다.

라이선스는 [AGPL-3.0](LICENSE)입니다. HMG-API 및 참고한 아이오닉 5 프로젝트의 출처는 [NOTICE.txt](NOTICE.txt)에 정리했습니다.
