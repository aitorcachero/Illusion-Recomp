/*
 * Illusion of Time — desktop host shim.
 *
 * Identity and hooks, nothing else. The host itself is the framework's
 * (snesrecomp/runner/src/desktop/host_main.h).
 */

#include "host_main.h"
#include "game_rtl.h"
#include "snesrecomp_rom_identity.h"  /* generated from rom_identity.txt */
#include "desktop/config.h"
#include "snes/ppu.h"
#include "snes/ws_shadow.h"
#include "widescreen.h"

#ifndef __ANDROID__
#define SDL_MAIN_HANDLED 1
#endif
#include "desktop/sdl_compat.h"
#ifdef __ANDROID__
/* SDLActivity loads libmain.so and dlsyms its SDL_main. */
#define main SDL_main
#endif

#ifndef SNES_GAME_VERSION
#define SNES_GAME_VERSION "dev"
#endif

extern Ppu *g_ppu;
extern uint8 g_ram[0x20000];

static const char *const kIllusionOfTimeAspectLabels[] = {
    "4:3 (Original Cl\xc3\xa1sico)",
    "16:9 Panor\xc3\xa1mico (Hack FOV)",
    "16:10 Panor\xc3\xa1mico (Steam Deck)",
    "Pantalla Completa (Estirado)",
};

static void IllusionOfTimePrepareFrame(int dw, int dh, int *w, int *h)
{
    (void)dw; (void)dh;
    if (g_config.display_aspect == 1) {
        *w = 384; /* 16:9 widescreen canvas: 256 + 2 * 64 */
    } else if (g_config.display_aspect == 2) {
        *w = 352; /* 16:10 widescreen canvas: 256 + 2 * 48 (Steam Deck 1280x800) */
    } else {
        *w = 256;
    }
    *h = 224;
}

static void IllusionOfTimeComputeViewport(int frame_w, int frame_h, int drawable_w, int drawable_h,
                                         SnesDisplayViewport *viewport)
{
    (void)frame_w; (void)frame_h;
    if (!viewport) return;
    if (g_config.display_aspect == 0) {
        /* 4:3 Original Clásico con barras laterales en pantallas panorámicas */
        *viewport = SnesDisplayAspect_FitViewport(4.0 / 3.0, drawable_w, drawable_h);
    } else if (g_config.display_aspect == 1) {
        /* 16:9 Panorámico con lienzo extendido a resolución nativa */
        *viewport = SnesDisplayAspect_FitViewport(16.0 / 9.0, drawable_w, drawable_h);
    } else if (g_config.display_aspect == 2) {
        /* 16:10 Panorámico nativo para Steam Deck (1280x800) y pantallas 16:10 */
        *viewport = SnesDisplayAspect_FitViewport(16.0 / 10.0, drawable_w, drawable_h);
    } else {
        /* Pantalla Completa estirada sin franjas */
        viewport->x = 0;
        viewport->y = 0;
        viewport->width = drawable_w > 0 ? drawable_w : 1;
        viewport->height = drawable_h > 0 ? drawable_h : 1;
    }
}

static void IllusionOfTimeBeginFrame(unsigned number)
{
    (void)number;
    if (!g_ppu) return;
    PpuSetPal(g_ppu, true);

    /* If mode is 4:3 or Stretched, disable extra wide PPU rasterization */
    if (g_config.display_aspect != 1 && g_config.display_aspect != 2 && !g_config.widescreen) {
        PpuSetExtraSpace(g_ppu, 0);
        return;
    }

    int budget = (g_config.display_aspect == 2) ? 48 : 64;
    PpuSetExtraSpaceCentered(g_ppu, budget);

    uint16_t camX   = (uint16_t)(g_ram[0x068A] | (g_ram[0x068B] << 8));
    uint16_t camY   = (uint16_t)(g_ram[0x068E] | (g_ram[0x068F] << 8));
    uint16_t mapW   = (uint16_t)(g_ram[0x0692] | (g_ram[0x0693] << 8));
    uint16_t mapH   = (uint16_t)(g_ram[0x0696] | (g_ram[0x0697] << 8));
    uint32_t mapPtr = (uint32_t)(g_ram[0x066C] | (g_ram[0x066D] << 8) | (g_ram[0x066E] << 16));
    int ppu_mode    = PPU_mode(g_ppu);

    /* Mode 5 and 6: High-resolution screens (dialogue/text/menus) */
    if (ppu_mode == 5 || ppu_mode == 6) {
        PpuSetExtraSideSpace(g_ppu, 0, 0, 0);
        WsShadowReset();
        return;
    }

    /* Mode 7 world map or special Mode 7 sequences */
    if (ppu_mode == 7) {
        PpuSetExtraSpace(g_ppu, budget);
        return;
    }

    /* If map width is 256 or less, or map height is 0 (menus, file select, Will's bedroom, intro logos),
     * pillarbox cleanly with pure black sidebars to prevent horizontal wrapping. */
    if (mapW <= 256 || mapH == 0) {
        PpuSetExtraSideSpace(g_ppu, 0, 0, 0);
        WsShadowReset();
        return;
    }

    /* Mode 1 exploration in large towns, rooms and dungeons:
     * Clamp camera margins to prevent rendering beyond authored map boundaries */
    /* budget already defined */
    int extraLeft = budget;
    int extraRight = budget;

    int maxCamX = (mapW > 256) ? (mapW - 256) : 0;
    extraLeft = (int)camX;
    if (extraLeft > budget) extraLeft = budget;
    if (extraLeft < 0) extraLeft = 0;

    int rightSpace = maxCamX - (int)camX;
    extraRight = rightSpace;
    if (extraRight > budget) extraRight = budget;
    if (extraRight < 0) extraRight = 0;

    if (extraLeft == 0 && extraRight == 0) {
        PpuSetExtraSideSpace(g_ppu, 0, 0, 0);
        WsShadowReset();
        return;
    }

    PpuSetExtraSideSpace(g_ppu, extraLeft, extraRight, 0);

    static uint32_t s_prev_map_ptr = 0;
    static uint16_t s_prev_mapW = 0;
    static uint16_t s_prev_mapH = 0;
    if (mapPtr != s_prev_map_ptr || mapW != s_prev_mapW || mapH != s_prev_mapH) {
        WsShadowReset();
        s_prev_map_ptr = mapPtr;
        s_prev_mapW = mapW;
        s_prev_mapH = mapH;
    }

    for (int l = 0; l < 2; l++) {
        WsShadowSetWorld(l, camX, camY);
        WsShadowSetScroll(l, g_ppu->hScroll[l], g_ppu->vScroll[l]);
        WsShadowSetRejectEastEcho(l, true);
    }
    WsShadowFrame(g_ppu);
    WsShadowExtendEdges(0, 64);
    WsShadowExtendEdges(1, 64);
}

static const SnesDesktopHostGame kGameHost = {
    .display_name        = "Illusion of Time",
    .window_title        = "Illusion of Time",
    .region              = SNESRECOMP_ROM_REGION,
    .rom_file            = SNESRECOMP_ROM_FILE,
    .expected_sha256_hex = SNESRECOMP_ROM_EXPECTED_SHA256,
    .expected_crc32_hex  = SNESRECOMP_ROM_EXPECTED_CRC32,
    .game_id             = SNESRECOMP_ROM_GAME_ID,
    .build_version       = SNES_GAME_VERSION,
    .game_info           = &kGameInfo,
    .num_players         = 2,
    /* Battery-backed SRAM shows the launcher's SAVES panel. Leave NULL for a
     * title without one. The path is exe-relative. */
    .sram_path           = "saves/save.srm",
    .widescreen_supported = 1,
    .native_widescreen   = 1,
    .display_aspect_supported = 1,
    .shader_supported    = 1,
    .aspect_labels       = kIllusionOfTimeAspectLabels,
    .num_aspect_labels   = 4,
    .aspect_setting_label = "Modo de Pantalla",
    .aspect_setting_help  = "4:3 clasico original, 16:9 Panoramico, 16:10 Steam Deck o Pantalla Completa estirada.",
    .prepare_frame       = IllusionOfTimePrepareFrame,
    .begin_sim_frame     = IllusionOfTimeBeginFrame,
    .compute_viewport    = IllusionOfTimeComputeViewport,
};

#ifndef __ANDROID__
#undef main   /* desktop: keep plain main() even if SDL_main.h remapped it */
#endif
int main(int argc, char **argv)
{
    return snesrecomp_desktop_main(&kGameHost, argc, argv);
}
