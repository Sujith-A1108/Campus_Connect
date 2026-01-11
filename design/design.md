Design tokens and screen mapping for Campus Connect

Colors
- colorPrimary: #2C7DAF
- colorPrimaryVariant: #0D3555
- appBarBlue: #2B76D2 (for Figma header)
- background: #FFFFFF
- textPrimary: #222222
- textSecondary: #666666

Typography
- Headline: 20sp bold
- Subhead: 14sp regular
- Body: 14sp regular

Spacing
- Small: 8dp
- Medium: 16dp
- Large: 24dp

Screen mapping (Figma -> Android)
- Splash -> SplashActivity (unchanged)
- Login -> `LoginActivity` & `activity_login.xml` (preserve behavior)
- Register -> `RegisterActivity` & `activity_register.xml` (preserve behavior)
- Home feed -> `HomeFragment` (`fragment_feed.xml`) using `AnnouncementAdapter` / `post_item.xml`
- College feed -> `CollegeFeedActivity` (`activity_college_feed.xml`)
- Post detail -> `AnnouncementsActivity` (detail view reuse)
- Profile -> `ProfileFragment` / `EditProfileActivity`
- Bottom navigation -> `activity_main.xml` with `BottomNavigationView`

Assets
- All uploaded icons should live in `app/src/main/res/drawable/`.
- Keep naming consistent: `ic_home`, `ic_search`, `ic_profile`, `ic_messages`, `ic_notifications`, `ic_compose`.

Notes
- Keep auth flow untouched (login/register logic remains the same).
- Use existing Firestore collections: `users`, `communities/{communityId}/announcements`.
- Backend moderation is handled by `functions/index.js` — do not remove or change its triggers.
