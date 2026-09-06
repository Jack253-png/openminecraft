#include "openminecraft/renderer/common/demiurge/node/controls/om_demiurge_button.hpp"
#include "openminecraft/renderer/common/animation/om_animation_easing.hpp"
#include "openminecraft/renderer/common/demiurge/node/om_demiurge_cliprect.hpp"
#include "openminecraft/renderer/common/demiurge/node/om_demiurge_container.hpp"
#include "openminecraft/renderer/common/demiurge/node/om_demiurge_rect.hpp"
#include "openminecraft/renderer/common/demiurge/node/om_demiurge_textsdf.hpp"
#include "openminecraft/renderer/common/demiurge/om_demiurge_geometry.hpp"
#include "openminecraft/renderer/common/demiurge/om_demiurge_node.hpp"
#include <array>
#include <SDL3/SDL.h>
#include <chrono>
#include <iostream>
#include <memory>
#include <thread>

namespace openminecraft::renderer::common::demiurge::node::controls
{
OMDemiurgeButton::OMDemiurgeButton(geom::OMFontSet *fontset) : opacityAnimated(0.6f)
{
    stylesStorage.put("justifyContent", OMDemiurgeAlign::Center);
    stylesStorage.put("alignItems", OMDemiurgeAlign::Center);
    bkgNode = std::make_shared<OMDemiurgeRectNode>()->style({
        {"radius", glm::vec4(20.0f)},
        {"position", Absolute},
        {"width", 100_percent},
        {"height", 100_percent},
    });
    textNode = std::make_shared<OMDemiurgeTextSdfNode>(fontset)->style({
        {"text", "Button"},
        {"textheight", 16},
        {"color", 0x000000ff},
        {"alignSelf", OMDemiurgeAlign::Center},
        {"margin", std::array<OMDemiurgeSize, 4>{5_px, 10_px, 5_px, 5_px}},
    });
    this->mountDirect(bkgNode);
    this->mountDirect(textNode);

    updateOpacity();
}
OMDemiurgeButton::~OMDemiurgeButton() = default;
void OMDemiurgeButton::update()
{
    updateOpacity();
    OMDemiurgeContainerNode::update();
}
auto OMDemiurgeButton::processMouseDown(float x, float y, uint8_t button) -> OMDemiurgeEventResult
{
    opacityAnimated.animateTo(1.0f, animation::easeOutCubic<float>, 0.2f);
    return Handled;
}
auto OMDemiurgeButton::processMouseUp(float x, float y, uint8_t button) -> OMDemiurgeEventResult
{
    opacityAnimated.animateTo(0.8f, animation::easeOutCubic<float>, 0.2f);
    return Handled;
}

auto OMDemiurgeButton::processMouseEnter(float x, float y) -> OMDemiurgeEventResult
{
    opacityAnimated.animateTo(0.8f, animation::easeOutCubic<float>, 0.2f);
    return Handled;
}
auto OMDemiurgeButton::processMouseExit(float x, float y) -> OMDemiurgeEventResult
{
    opacityAnimated.animateTo(0.6f, animation::easeOutCubic<float>, 1.0f);
    return Handled;
}

auto OMDemiurgeButton::submit(OMDemiurgeRendererHandler *handler, float depth) -> void
{
    OMDemiurgeContainerNode::submit(handler, depth);
}

void OMDemiurgeButton::updateOpacity()
{
    bkgNode->style("color", (int)(0xefefef00 | static_cast<uint8_t>(opacityAnimated.get() * 255.0f)));
}

void OMDemiurgeButton::setText(std::string s)
{
    textNode->style("text", s);
}
void OMDemiurgeButton::setTextColor(int c)
{
    textNode->style("color", c);
}
void OMDemiurgeButton::setBackgroundColor(int c)
{
    bkgNode->style("color", c);
}
void OMDemiurgeButton::setBackgroundRadius(glm::vec4 r)
{
    bkgNode->style("radius", r);
}
void OMDemiurgeButton::setOnClick(std::function<void()> h)
{
    handle = h;
}
} // namespace openminecraft::renderer::common::demiurge::node::controls
