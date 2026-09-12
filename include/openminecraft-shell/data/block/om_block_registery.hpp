#ifndef OM_BLOCK_REGISTERY_HPP
#define OM_BLOCK_REGISTERY_HPP

#include "openminecraft-shell/data/block/om_block.hpp"
#include "openminecraft-shell/data/om_identifier.hpp"
#include "openminecraft/world/om_world_registry.hpp"
#include <memory>

using namespace openminecraft::world;

namespace openminecraftshell::data::block
{
extern OMWorldRegistry<OMIdentifier, std::shared_ptr<OMBlock>> blockRegistery;
void registerBlocks();
} // namespace openminecraftshell::data::block

#endif